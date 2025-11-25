package com.example.uniforlibrary.produzir

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.uniforlibrary.R
import com.example.uniforlibrary.repository.RatingRepository
import com.example.uniforlibrary.repository.ReadingProgressRepository
import com.example.uniforlibrary.service.CloudinaryService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs

class PdfReaderActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PdfReaderActivity"
        const val EXTRA_PDF_URL = "pdf_url"
        const val EXTRA_TITLE = "title"
        const val EXTRA_SHOW_RATING = "show_rating"
        const val EXTRA_PRODUCAO_ID = "producao_id"

        fun start(context: Context, pdfUrl: String, title: String, showRating: Boolean = false, producaoId: String? = null) {
            val intent = Intent(context, PdfReaderActivity::class.java).apply {
                putExtra(EXTRA_PDF_URL, pdfUrl)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_SHOW_RATING, showRating)
                putExtra(EXTRA_PRODUCAO_ID, producaoId)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var progressBar: ProgressBar
    private lateinit var pageNumberText: TextView
    private lateinit var controlsLayout: LinearLayout
    private lateinit var prevPageButton: ImageButton
    private lateinit var nextPageButton: ImageButton
    private lateinit var zoomInButton: ImageButton
    private lateinit var zoomOutButton: ImageButton
    private lateinit var brightnessButton: ImageButton
    private lateinit var bookmarkButton: ImageButton
    private lateinit var ratingLayout: LinearLayout
    private lateinit var progressText: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var sendRatingButton: Button
    private lateinit var skipRatingButton: Button

    // Novo: ImageView para exibir páginas renderizadas
    private lateinit var pdfImageView: ImageView

    private var currentPage = 0
    private var totalPages = 0
    private var currentZoom = 1.0f
    private var pdfUrl: String = ""
    private var bookTitle: String = ""
    private var showRating: Boolean = false
    private var producaoId: String? = null
    private var isBookmarked = false

    private var pdfRenderer: PdfRenderer? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null

    // Variáveis para pan/arrastar
    private val imageMatrix = Matrix()
    private val lastTouchPoint = PointF()
    private var isDragging = false
    private val MIN_DRAG_DISTANCE = 10f // pixels mínimos para considerar drag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_reader)

        pdfUrl = intent.getStringExtra(EXTRA_PDF_URL) ?: ""
        bookTitle = intent.getStringExtra(EXTRA_TITLE) ?: "Leitura"
        showRating = intent.getBooleanExtra(EXTRA_SHOW_RATING, false)
        producaoId = intent.getStringExtra(EXTRA_PRODUCAO_ID)

        Log.d(TAG, "onCreate - pdfUrl=$pdfUrl, title=$bookTitle, showRating=$showRating, producaoId=$producaoId")


        if (pdfUrl.isBlank()) {
            Toast.makeText(this, "Erro: URL do PDF vazia", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initViews()
        setupToolbar()
        setupControls()
        loadPdf()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        progressBar = findViewById(R.id.progressBar)
        pageNumberText = findViewById(R.id.pageNumberText)
        controlsLayout = findViewById(R.id.controlsLayout)
        prevPageButton = findViewById(R.id.prevPageButton)
        nextPageButton = findViewById(R.id.nextPageButton)
        zoomInButton = findViewById(R.id.zoomInButton)
        zoomOutButton = findViewById(R.id.zoomOutButton)
        brightnessButton = findViewById(R.id.brightnessButton)
        bookmarkButton = findViewById(R.id.bookmarkButton)
        ratingLayout = findViewById(R.id.ratingLayout)
        ratingBar = findViewById(R.id.ratingBar)
        sendRatingButton = findViewById(R.id.sendRatingButton)
        skipRatingButton = findViewById(R.id.skipRatingButton)
        progressText = findViewById(R.id.progressText)

        // Substitui o antigo PDFView por um ImageView simples
        pdfImageView = ImageView(this).apply {
            adjustViewBounds = false
            scaleType = ImageView.ScaleType.MATRIX // Permite controle manual via Matrix
        }
        val container: FrameLayout = findViewById(R.id.pdfViewContainer)
        container.addView(
            pdfImageView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // Adicionar touch listener para pan/arrastar
        setupTouchListener()
    }

    private fun setupTouchListener() {
        pdfImageView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchPoint.set(event.x, event.y)
                    isDragging = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - lastTouchPoint.x
                    val dy = event.y - lastTouchPoint.y

                    if (abs(dx) > MIN_DRAG_DISTANCE || abs(dy) > MIN_DRAG_DISTANCE) {
                        isDragging = true
                    }

                    if (isDragging && currentZoom > 1.0f) {
                        // Permite arrastar quando zoom > 1
                        imageMatrix.postTranslate(dx, dy)
                        pdfImageView.imageMatrix = imageMatrix
                        lastTouchPoint.set(event.x, event.y)
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging && currentZoom <= 1.0f) {
                        // Se não arrastou e zoom normal, navega entre páginas
                        val x = event.x
                        if (x < view.width / 2) {
                            // Toque na esquerda = página anterior
                            if (currentPage > 0) {
                                renderPage(currentPage - 1)
                            }
                        } else {
                            // Toque na direita = próxima página
                            if (currentPage < totalPages - 1) {
                                renderPage(currentPage + 1)
                            }
                        }
                    }
                    isDragging = false
                    true
                }

                else -> false
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = bookTitle
        }
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupControls() {
        // Navegação de páginas
        prevPageButton.setOnClickListener {
            if (currentPage > 0) {
                renderPage(currentPage - 1)
            }
        }

        nextPageButton.setOnClickListener {
            if (currentPage < totalPages - 1) {
                renderPage(currentPage + 1)
            }
        }

        // Zoom controls com Matrix
        zoomInButton.setOnClickListener {
            if (currentZoom < 3.0f) {
                currentZoom += 0.25f
                applyZoom()
            }
        }

        zoomOutButton.setOnClickListener {
            if (currentZoom > 0.5f) {
                currentZoom -= 0.25f
                applyZoom()
            }
        }

        // Brightness control
        var currentBrightnessIndex = 0
        val brightnessLevels = listOf(0.3f, 0.6f, 1.0f)
        brightnessButton.setOnClickListener {
            currentBrightnessIndex = (currentBrightnessIndex + 1) % brightnessLevels.size
            val layoutParams = window.attributes
            layoutParams.screenBrightness = brightnessLevels[currentBrightnessIndex]
            window.attributes = layoutParams

            Toast.makeText(
                this,
                "Brilho: ${(brightnessLevels[currentBrightnessIndex] * 100).toInt()}%",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Bookmark
        bookmarkButton.setOnClickListener {
            isBookmarked = !isBookmarked
            bookmarkButton.setImageResource(
                if (isBookmarked) android.R.drawable.star_on else android.R.drawable.star_off
            )
            Toast.makeText(
                this,
                if (isBookmarked) "Marcador adicionado na página ${currentPage + 1}" else "Marcador removido",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Rating controls
        sendRatingButton.setOnClickListener {
            val rating = ratingBar.rating
            if (rating > 0) {
                // Salvar avaliação no Firebase
                if (producaoId != null) {
                    val auth = FirebaseAuth.getInstance()
                    val currentUser = auth.currentUser

                    if (currentUser != null) {
                        val ratingRepository = RatingRepository()

                        CoroutineScope(Dispatchers.IO).launch {
                            val result = ratingRepository.createProducaoRating(
                                producaoId = producaoId!!,
                                userId = currentUser.uid,
                                userName = currentUser.displayName ?: currentUser.email ?: "Usuário",
                                stars = rating.toInt(),
                                comment = ""
                            )

                            withContext(Dispatchers.Main) {
                                result.onSuccess {
                                    Toast.makeText(
                                        this@PdfReaderActivity,
                                        "Avaliação de $rating estrelas enviada com sucesso!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    ratingLayout.isVisible = false
                                    Log.d(TAG, "✅ Avaliação salva: $rating estrelas para produção $producaoId")
                                }.onFailure { e ->
                                    Toast.makeText(
                                        this@PdfReaderActivity,
                                        "Erro ao enviar avaliação: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    Log.e(TAG, "❌ Erro ao salvar avaliação", e)
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Você precisa estar logado para avaliar", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Avaliação de $rating estrelas enviada!", Toast.LENGTH_SHORT).show()
                    ratingLayout.isVisible = false
                    Log.w(TAG, "⚠️ producaoId é null - avaliação não foi salva")
                }
            } else {
                Toast.makeText(this, "Por favor, selecione uma avaliação", Toast.LENGTH_SHORT).show()
            }
        }

        skipRatingButton.setOnClickListener {
            ratingLayout.isVisible = false
        }
    }

    private fun loadPdf() {
        progressBar.isVisible = true
        Log.d(TAG, "loadPdf - url=$pdfUrl")

        if (pdfUrl.startsWith("http")) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val file = downloadPdf(pdfUrl)
                    Log.d(TAG, "PDF baixado em: ${file.absolutePath}")
                    withContext(Dispatchers.Main) {
                        openPdf(file)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao baixar PDF", e)
                    withContext(Dispatchers.Main) {
                        progressBar.isVisible = false
                        Toast.makeText(
                            this@PdfReaderActivity,
                            "Erro ao carregar PDF: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }
        } else {
            val file = File(pdfUrl)
            Log.d(TAG, "Abrindo PDF local: ${file.absolutePath}")
            if (file.exists()) {
                openPdf(file)
            } else {
                progressBar.isVisible = false
                Toast.makeText(this, "Arquivo não encontrado", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private suspend fun downloadPdf(urlString: String): File = withContext(Dispatchers.IO) {
        Log.d(TAG, "")
        Log.d(TAG, "═══════════════════════════════════════════")
        Log.d(TAG, "    INICIANDO DOWNLOAD DE PDF")
        Log.d(TAG, "═══════════════════════════════════════════")
        Log.d(TAG, "URL original: $urlString")
        Log.d(TAG, "")

        // Lista de URLs alternativas para tentar
        val urlsToTry = mutableListOf(urlString)

        // Se a URL usa /raw/upload/, tentar também com /image/upload/
        if (urlString.contains("/raw/upload/")) {
            val imageUrl = urlString.replace("/raw/upload/", "/image/upload/")
            urlsToTry.add(imageUrl)
            Log.d(TAG, "✓ Alternativa 1: /image/upload/")
        }

        // Tentar sem versionamento (sem /v12345/)
        if (urlString.contains(Regex("/v\\d+/"))) {
            val urlWithoutVersion = urlString.replace(Regex("/v\\d+/"), "/")
            urlsToTry.add(urlWithoutVersion)
            Log.d(TAG, "✓ Alternativa 2: sem versionamento")
        }

        // Se for URL do Cloudinary, preparar URL assinada como última tentativa
        var signedUrl: String? = null
        if (urlString.contains("cloudinary.com")) {
            try {
                val publicId = CloudinaryService.extractPublicId(urlString)
                if (publicId != null) {
                    signedUrl = CloudinaryService.generateSignedUrl(publicId, "raw")
                    Log.d(TAG, "✓ Alternativa 3: URL assinada (para arquivos privados)")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Não foi possível gerar URL assinada: ${e.message}")
            }
        }

        Log.d(TAG, "")
        Log.d(TAG, "Total de URLs para tentar: ${urlsToTry.size}${if (signedUrl != null) " + 1 assinada" else ""}")
        Log.d(TAG, "───────────────────────────────────────────")

        val results = mutableListOf<String>()
        var got401 = false

        for ((index, currentUrl) in urlsToTry.withIndex()) {
            try {
                Log.d(TAG, "")
                Log.d(TAG, "┌─────────────────────────────────────────┐")
                Log.d(TAG, "│  Tentativa ${index + 1} de ${urlsToTry.size}")
                Log.d(TAG, "└─────────────────────────────────────────┘")
                Log.d(TAG, "URL: $currentUrl")

                val url = URL(currentUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "UniforLibrary-App/1.0")

                Log.d(TAG, "Conectando...")
                connection.connect()

                val responseCode = connection.responseCode
                val responseMessage = connection.responseMessage
                val contentType = connection.contentType
                val contentLength = connection.contentLength

                Log.d(TAG, "")
                Log.d(TAG, "Resposta recebida:")
                Log.d(TAG, "  ├─ Status: $responseCode $responseMessage")
                Log.d(TAG, "  ├─ Content-Type: ${contentType ?: "não informado"}")
                Log.d(TAG, "  └─ Content-Length: ${if (contentLength > 0) "$contentLength bytes" else "não informado"}")

                results.add("Tentativa ${index + 1}: HTTP $responseCode ($responseMessage)")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "")
                    Log.d(TAG, "✅ Status 200 OK! Baixando arquivo...")

                    val inputStream = connection.inputStream
                    val file = File(cacheDir, "temp_${System.currentTimeMillis()}.pdf")

                    var bytesDownloaded = 0
                    FileOutputStream(file).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            bytesDownloaded += bytesRead
                        }
                    }
                    inputStream.close()

                    Log.d(TAG, "")
                    Log.d(TAG, "╔═══════════════════════════════════════╗")
                    Log.d(TAG, "║        DOWNLOAD CONCLUÍDO!            ║")
                    Log.d(TAG, "╚═══════════════════════════════════════╝")
                    Log.d(TAG, "Arquivo salvo: ${file.absolutePath}")
                    Log.d(TAG, "Tamanho: ${file.length()} bytes")
                    Log.d(TAG, "Bytes baixados: $bytesDownloaded")
                    Log.d(TAG, "URL usada: $currentUrl")
                    Log.d(TAG, "")

                    return@withContext file
                } else {
                    Log.w(TAG, "")
                    Log.w(TAG, "❌ Falhou: HTTP $responseCode")

                    if (responseCode == 404) {
                        Log.w(TAG, "→ Arquivo não encontrado (404)")
                    } else if (responseCode == 403) {
                        Log.w(TAG, "→ Acesso negado (403)")
                    } else if (responseCode == 401) {
                        Log.w(TAG, "→ Não autorizado (401) - Arquivo privado!")
                        got401 = true
                    }

                    Log.w(TAG, "Tentando próxima URL...")
                }

                connection.disconnect()

            } catch (e: Exception) {
                Log.e(TAG, "")
                Log.e(TAG, "❌ EXCEÇÃO na tentativa ${index + 1}")
                Log.e(TAG, "Tipo: ${e.javaClass.simpleName}")
                Log.e(TAG, "Mensagem: ${e.message}", e)
                results.add("Tentativa ${index + 1}: Erro - ${e.javaClass.simpleName}: ${e.message}")
            }
        }

        // Se detectou 401 e temos URL assinada, tentar como última opção
        if (got401 && signedUrl != null) {
            try {
                Log.d(TAG, "")
                Log.d(TAG, "┌─────────────────────────────────────────┐")
                Log.d(TAG, "│  Tentativa EXTRA: URL Assinada")
                Log.d(TAG, "└─────────────────────────────────────────┘")
                Log.d(TAG, "URL: $signedUrl")
                Log.d(TAG, "Conectando...")

                val url = URL(signedUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "UniforLibrary-App/1.0")

                connection.connect()

                val responseCode = connection.responseCode
                val responseMessage = connection.responseMessage
                val contentType = connection.contentType
                val contentLength = connection.contentLength

                Log.d(TAG, "")
                Log.d(TAG, "Resposta recebida:")
                Log.d(TAG, "  ├─ Status: $responseCode $responseMessage")
                Log.d(TAG, "  ├─ Content-Type: ${contentType ?: "não informado"}")
                Log.d(TAG, "  └─ Content-Length: ${if (contentLength > 0) "$contentLength bytes" else "não informado"}")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "")
                    Log.d(TAG, "✅ Status 200 OK com URL assinada! Baixando arquivo...")

                    val inputStream = connection.inputStream
                    val file = File(cacheDir, "temp_${System.currentTimeMillis()}.pdf")

                    var bytesDownloaded = 0
                    FileOutputStream(file).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            bytesDownloaded += bytesRead
                        }
                    }
                    inputStream.close()

                    Log.d(TAG, "")
                    Log.d(TAG, "╔═══════════════════════════════════════╗")
                    Log.d(TAG, "║   DOWNLOAD CONCLUÍDO (URL ASSINADA)!  ║")
                    Log.d(TAG, "╚═══════════════════════════════════════╝")
                    Log.d(TAG, "Arquivo salvo: ${file.absolutePath}")
                    Log.d(TAG, "Tamanho: ${file.length()} bytes")
                    Log.d(TAG, "")

                    return@withContext file
                } else {
                    Log.w(TAG, "❌ URL assinada também falhou: HTTP $responseCode")
                    results.add("URL Assinada: HTTP $responseCode ($responseMessage)")
                }

                connection.disconnect()

            } catch (e: Exception) {
                Log.e(TAG, "❌ EXCEÇÃO na URL assinada: ${e.javaClass.simpleName}", e)
                results.add("URL Assinada: Erro - ${e.message}")
            }
        }

        // Se chegou aqui, todas as tentativas falharam
        Log.e(TAG, "")
        Log.e(TAG, "╔═══════════════════════════════════════╗")
        Log.e(TAG, "║   TODAS AS TENTATIVAS FALHARAM!!!     ║")
        Log.e(TAG, "╚═══════════════════════════════════════╝")
        Log.e(TAG, "")
        Log.e(TAG, "Resumo das tentativas:")
        results.forEachIndexed { idx, result ->
            Log.e(TAG, "  ${idx + 1}. $result")
        }
        Log.e(TAG, "")
        Log.e(TAG, "URLs tentadas:")
        urlsToTry.forEachIndexed { idx, url ->
            Log.e(TAG, "  ${idx + 1}. $url")
        }
        Log.e(TAG, "═══════════════════════════════════════════")

        throw Exception(
            "PDF não encontrado após ${urlsToTry.size} tentativas.\n\n" +
            "Verifique no Logcat (filtro: PdfReaderActivity) os detalhes de cada tentativa.\n\n" +
            "Possíveis causas:\n" +
            "• URL incorreta no Firestore\n" +
            "• Arquivo não foi enviado para o Cloudinary\n" +
            "• Arquivo foi excluído\n\n" +
            "Tente reenviar o PDF."
        )
    }

    private fun openPdf(file: File) {
        try {
            Log.d(TAG, "openPdf - file=${file.absolutePath}, size=${file.length()}")
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            parcelFileDescriptor?.let { pfd ->
                pdfRenderer = PdfRenderer(pfd)
                totalPages = pdfRenderer?.pageCount ?: 0
                Log.d(TAG, "PDF aberto com $totalPages páginas")
                if (totalPages == 0) {
                    progressBar.isVisible = false
                    Toast.makeText(this, "PDF sem páginas", Toast.LENGTH_LONG).show()
                    finish()
                    return
                }
                currentPage = 0
                renderPage(currentPage)
                progressBar.isVisible = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir PDF", e)
            progressBar.isVisible = false
            Toast.makeText(this, "Erro ao abrir PDF: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun renderPage(index: Int) {
        val renderer = pdfRenderer ?: run {
            Log.e(TAG, "renderPage - pdfRenderer nulo")
            return
        }
        if (index < 0 || index >= renderer.pageCount) {
            Log.e(TAG, "renderPage - índice inválido: $index")
            return
        }

        Log.d(TAG, "renderPage - página $index de ${renderer.pageCount}")
        val page = renderer.openPage(index)
        val width = resources.displayMetrics.densityDpi / 72 * page.width
        val height = resources.displayMetrics.densityDpi / 72 * page.height
        Log.d(TAG, "renderPage - width=$width, height=$height")
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()

        pdfImageView.setImageBitmap(bitmap)

        // Resetar zoom ao mudar de página
        currentZoom = 1.0f
        applyZoom()

        currentPage = index
        updatePageNumber()

        // Marcar leitura como completa quando chegar à última página
        if (showRating && currentPage == totalPages - 1 && producaoId != null) {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val readingProgressRepository = ReadingProgressRepository()

                CoroutineScope(Dispatchers.IO).launch {
                    // Marcar leitura como completa
                    readingProgressRepository.markAsCompleted(producaoId!!, currentUser.uid)

                    withContext(Dispatchers.Main) {
                        // Mostrar diálogo de avaliação após 2 segundos
                        ratingLayout.postDelayed({
                            ratingLayout.isVisible = true
                            Toast.makeText(
                                this@PdfReaderActivity,
                                "Parabéns! Você completou a leitura! 🎉",
                                Toast.LENGTH_LONG
                            ).show()
                        }, 2000)
                    }
                }
            }
        }
    }

    /**
     * Aplica zoom usando Matrix
     * Centraliza a imagem ao dar zoom
     */
    private fun applyZoom() {
        val drawable = pdfImageView.drawable ?: return

        imageMatrix.reset()

        val viewWidth = pdfImageView.width.toFloat()
        val viewHeight = pdfImageView.height.toFloat()
        val drawableWidth = drawable.intrinsicWidth.toFloat()
        val drawableHeight = drawable.intrinsicHeight.toFloat()

        if (viewWidth == 0f || viewHeight == 0f || drawableWidth == 0f || drawableHeight == 0f) {
            // Aguardar layout estar pronto
            pdfImageView.post { applyZoom() }
            return
        }

        // Calcular escala para fit center
        val scale = Math.min(viewWidth / drawableWidth, viewHeight / drawableHeight)

        // Aplicar escala base + zoom
        val finalScale = scale * currentZoom

        // Calcular posição para centralizar
        val scaledWidth = drawableWidth * finalScale
        val scaledHeight = drawableHeight * finalScale
        val dx = (viewWidth - scaledWidth) / 2f
        val dy = (viewHeight - scaledHeight) / 2f

        // Aplicar transformações: escala + centralização
        imageMatrix.postScale(finalScale, finalScale)
        imageMatrix.postTranslate(dx, dy)

        pdfImageView.imageMatrix = imageMatrix

        Log.d(TAG, "applyZoom - zoom=$currentZoom, finalScale=$finalScale")

        // Mostrar mensagem se zoom > 1
        if (currentZoom > 1.0f) {
            Toast.makeText(this, "Zoom ${String.format("%.0f", currentZoom * 100)}% - Arraste para mover", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePageNumber() {
        pageNumberText.text = "Página ${currentPage + 1} de $totalPages"

        // Calcular e exibir porcentagem
        val percentage = if (totalPages > 0) {
            ((currentPage + 1) * 100) / totalPages
        } else {
            0
        }
        progressText.text = "$percentage%"

        // Atualizar estado dos botões
        prevPageButton.isEnabled = currentPage > 0
        nextPageButton.isEnabled = currentPage < totalPages - 1

        prevPageButton.alpha = if (currentPage > 0) 1.0f else 0.5f
        nextPageButton.alpha = if (currentPage < totalPages - 1) 1.0f else 0.5f

        Log.d(TAG, "updatePageNumber - Página ${currentPage + 1}/$totalPages ($percentage%)")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
    }
}
