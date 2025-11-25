# 📚 Unifor Library App

<p align="center">
  <img src="https://img.shields.io/badge/Android-36-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android API 26+">
  <img src="https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack_Compose-1.7-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Firebase-Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase">
  <img src="https://img.shields.io/badge/Material_3-Design-6200EE?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material Design 3">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Gemini_AI-Chatbot-8E75B2?style=for-the-badge&logo=googlegemini&logoColor=white" alt="Gemini AI">
  <img src="https://img.shields.io/badge/Cloudinary-Media-3448C5?style=for-the-badge&logo=cloudinary&logoColor=white" alt="Cloudinary">
  <img src="https://img.shields.io/badge/MVVM-Architecture-00897B?style=for-the-badge&logo=android&logoColor=white" alt="MVVM">
</p>

---

## 🎯 Visão Geral

O **Unifor Library App** é um sistema completo de gerenciamento de biblioteca universitária desenvolvido em **Kotlin** com **Jetpack Compose**. O aplicativo oferece uma experiência moderna e intuitiva para estudantes e administradores, permitindo gerenciar acervos, empréstimos, reservas, exposições e muito mais.

### ✨ Destaques do Projeto

- 🎨 **Interface Moderna**: Design baseado em Material Design 3 com Jetpack Compose
- 🤖 **IA Integrada**: Chatbot inteligente powered by Google Gemini AI
- 🔐 **Autenticação Segura**: Sistema completo com Firebase Authentication
- 📊 **Painel Administrativo**: Gerenciamento completo do acervo e relatórios
- 📱 **Experiência Fluida**: Navegação intuitiva com animações suaves
- ☁️ **Cloud Storage**: Armazenamento de imagens via Cloudinary

---

## 🚀 Principais Funcionalidades

### 👨‍🎓 Para Usuários (Estudantes)

| Funcionalidade | Descrição |
|---------------|-----------|
| 📖 **Acervo Digital** | Busca e visualização completa do catálogo de livros |
| 🔍 **Busca Inteligente** | Pesquisa por título, autor ou categoria em tempo real |
| 📅 **Reservas** | Sistema de reserva de livros físicos |
| 🏷️ **Empréstimos** | Acompanhamento de empréstimos ativos e histórico |
| 🎭 **Exposições** | Acesso a exposições virtuais e eventos culturais |
| 📝 **Leitura Digital** | Visualização de PDFs e conteúdo digital |
| 💬 **Chatbot IA** | Assistente virtual com Gemini AI para suporte |
| 🔔 **Notificações** | Alertas sobre prazos, renovações e novidades |
| 👤 **Perfil** | Gerenciamento de dados pessoais e preferências |
| ✍️ **Produção Acadêmica** | Submissão de trabalhos e produções |

### 👨‍💼 Para Administradores

| Funcionalidade | Descrição |
|---------------|-----------|
| 📚 **Gestão de Acervo** | CRUD completo de livros com upload de capas |
| 📊 **Relatórios** | Dashboards e analytics detalhados |
| 🎫 **Gestão de Reservas** | Aprovação e controle de reservas |
| 📋 **Controle de Empréstimos** | Gerenciamento de empréstimos e devoluções |
| 🎨 **Gestão de Exposições** | Criação e gerenciamento de eventos |
| 📈 **Analytics** | Métricas de uso e estatísticas |

---

## 🏗️ Arquitetura e Tecnologias

### 📐 Arquitetura

```
📦 Unifor Library App
├── 🎨 UI Layer (Jetpack Compose)
│   ├── Material Design 3 Components
│   ├── Navigation Component
│   └── Custom Components
│
├── 🧠 ViewModel Layer (MVVM)
│   ├── BookViewModel
│   ├── AuthViewModel
│   ├── ExposicoesViewModel
│   └── ReservationViewModel
│
├── 📂 Repository Layer
│   ├── BookRepository
│   ├── ProfileRepository
│   └── Data Sources
│
└── 🔥 Data Layer
    ├── Firebase Firestore
    ├── Firebase Auth
    └── Cloudinary Service
```

### 🛠️ Stack Tecnológico

#### **Frontend**
- **Jetpack Compose**: UI declarativa moderna
- **Material Design 3**: Componentes e design system
- **Coil**: Carregamento otimizado de imagens
- **Navigation Compose**: Navegação type-safe

#### **Backend & Serviços**
- **Firebase Authentication**: Gerenciamento de usuários
- **Firebase Firestore**: Banco de dados NoSQL em tempo real
- **Google Gemini AI**: Chatbot inteligente
- **Cloudinary**: CDN e otimização de imagens

#### **Arquitetura & Padrões**
- **MVVM**: Model-View-ViewModel
- **Repository Pattern**: Abstração de fonte de dados
- **Kotlin Flows**: Programação reativa
- **StateFlow**: Gerenciamento de estado
- **Coroutines**: Operações assíncronas

---

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- ✅ [Android Studio](https://developer.android.com/studio) Hedgehog ou superior
- ✅ JDK 11 ou superior
- ✅ SDK Android (API 26+)
- ✅ Git
- ✅ Conta no [Firebase](https://firebase.google.com/)
- ✅ Conta no [Cloudinary](https://cloudinary.com/)
- ✅ API Key do [Google Gemini](https://makersuite.google.com/app/apikey)

---

## ⚙️ Instalação e Configuração

### 1️⃣ Clone o Repositório

```bash
git clone https://github.com/seu-usuario/library-app.git
cd library-app
```

### 2️⃣ Configuração do Firebase

1. Acesse o [Firebase Console](https://console.firebase.google.com/)
2. Crie um novo projeto ou use um existente
3. Adicione um aplicativo Android com o package: `com.example.uniforlibrary`
4. Baixe o arquivo `google-services.json`
5. Coloque-o em: `app/google-services.json`

#### Configure o Firestore:

```javascript
// Coleções necessárias:
- books         // Acervo de livros
- users         // Usuários
- loans         // Empréstimos
- reservations  // Reservas
- exhibitions   // Exposições
- notifications // Notificações
```

### 3️⃣ Configuração de APIs

Crie o arquivo `local.properties` na raiz do projeto:

```properties
# Cloudinary
cloudinary.cloud.name=seu_cloud_name
cloudinary.api.key=sua_api_key
cloudinary.api.secret=seu_api_secret

# Google Gemini AI
gemini.api.key=sua_gemini_api_key
```

⚠️ **IMPORTANTE**: Nunca commite o arquivo `local.properties`! Ele já está no `.gitignore`.

### 4️⃣ Sincronize o Projeto

No Android Studio:
1. Abra o projeto
2. Aguarde a sincronização do Gradle
3. Execute: **Build > Make Project**

### 5️⃣ Execute o Aplicativo

```bash
# Via linha de comando
./gradlew installDebug

# Ou no Android Studio
# Clique no botão Run ▶️
```

---

## 📱 Screenshots & Demonstração

### 🔐 Login (Admin e Usuário)
<p align="center">
  <img src="https://github.com/user-attachments/assets/30d8f593-b365-433f-905e-cebaffcd9121" alt="Tela de Login" width="250"/>
</p>

---

### 🛠️ Painel Administrativo

<p align="center">
  <img src="https://github.com/user-attachments/assets/2a4c07d4-cdc3-4c1a-84d6-c1571ee8f698" alt="Início Admin" width="250"/>
  <img src="https://github.com/user-attachments/assets/cdcb4cb9-78cb-4d0d-ba0a-39e645745fbe" alt="Acervo Admin" width="250"/>
  <img src="https://github.com/user-attachments/assets/707da765-f320-4321-8091-3b925d8579bf" alt="Exposição Admin" width="250"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/5cfab6ea-97c8-4c55-8d2c-ba7e91116096" alt="Ver Exposição Admin" width="250"/>
  <img src="https://github.com/user-attachments/assets/63e59f01-ab91-455c-8b52-136822328c9e" alt="Reservas Admin" width="250"/>
  <img src="https://github.com/user-attachments/assets/d400c792-1b6c-49f7-9c8b-4ace566249da" alt="Relatórios Admin" width="250"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/9c0d72eb-23db-4b80-aaf2-eb83100d2f22" alt="Notificação" width="250"/>
</p>

---

### 👥 Área do Usuário

<p align="center">
  <img src="https://github.com/user-attachments/assets/b2bafca9-8d9d-449f-a99e-0b855ae359fd" alt="Início Usuário" width="250"/>
  <img src="https://github.com/user-attachments/assets/12548ad3-ca24-4328-8c02-e9d69d4d13b4" alt="Exposição Usuário" width="250"/>
  <img src="https://github.com/user-attachments/assets/fef55ed4-aa51-4eec-82d8-6a7631c6abc9" alt="Reservas Usuário" width="250"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/9fc54978-79f9-46cf-bb06-a5d3365bf84f" alt="Empréstimo Usuário" width="250"/>
  <img src="https://github.com/user-attachments/assets/91c01a10-0803-46b8-9721-6ad466182da4" alt="Acervo Usuário" width="250"/>
</p>

---

## 📂 Estrutura do Projeto

```
app/src/main/java/com/example/uniforlibrary/
│
├── 📱 acervo/                    # Catálogo de livros
│   ├── AcervoActivity.kt
│   └── BookDetailActivity.kt
│
├── 👨‍💼 acervoAdm/                 # Admin - Gestão de livros
│
├── 💬 chatbot/                   # Chatbot com IA
│   └── ChatActivity.kt
│
├── 🧩 components/                # Componentes reutilizáveis
│   ├── Chatbot.kt
│   └── UserBottomNav.kt
│
├── 📋 emprestimos/               # Sistema de empréstimos
│   ├── EmprestimosActivity.kt
│   └── EmprestimoDetailActivity.kt
│
├── 🎭 exposicoes/                # Exposições e eventos
│   ├── ExposicoesActivity.kt
│   ├── ExhibitionDetailActivity.kt
│   └── ReadingActivity.kt
│
├── 🏠 home/                      # Tela inicial
│   └── HomeActivity.kt
│
├── 🔐 login/                     # Autenticação
│   ├── LoginActivity.kt
│   ├── RegisterActivity.kt
│   └── ForgotPasswordActivity.kt
│
├── 📦 model/                     # Modelos de dados
│   ├── Book.kt
│   ├── Loan.kt
│   ├── Reservation.kt
│   └── Producao.kt
│
├── 🔔 notificacoes/             # Sistema de notificações
│
├── 👤 profile/                   # Perfil do usuário
│   ├── EditProfileActivity.kt
│   └── ProfileRepository.kt
│
├── 📂 repository/                # Camada de dados
│   └── BookRepository.kt
│
├── 📅 reservation/               # Sistema de reservas
│   ├── MyReservationsActivity.kt
│   └── UserReservationsActivity.kt
│
├── ☁️ service/                   # Serviços externos
│   └── CloudinaryService.kt
│
├── 🎨 ui/theme/                  # Tema e design
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
│
└── 🧠 viewmodel/                 # ViewModels (MVVM)
    ├── AuthViewModel.kt
    ├── BookViewModel.kt
    ├── ExposicoesViewModel.kt
    └── UserReservationViewModel.kt
```

---

## 🎯 Como Usar

### Para Estudantes

1. **Cadastro/Login**
    - Abra o app e crie sua conta com e-mail institucional
    - Ou faça login se já tiver uma conta

2. **Explorar o Acervo**
    - Na tela inicial, navegue pelas categorias
    - Use a busca para encontrar livros específicos
    - Veja detalhes, avaliações e disponibilidade

3. **Fazer Reservas**
    - Selecione um livro disponível
    - Clique em "Reservar"
    - Retire na biblioteca no prazo especificado

4. **Usar o Chatbot**
    - Clique no ícone do chat flutuante
    - Faça perguntas sobre livros, horários, etc.
    - O assistente IA responderá instantaneamente

### Para Administradores

1. **Login Administrativo**
    - Use credenciais de administrador

2. **Gerenciar Acervo**
    - Adicione novos livros com fotos
    - Edite informações existentes
    - Controle disponibilidade

3. **Acompanhar Métricas**
    - Acesse relatórios detalhados
    - Veja livros mais emprestados
    - Monitore reservas pendentes

---

## 🚀 Build e Deploy

### Build Debug

```bash
./gradlew assembleDebug
```

### Build Release

```bash
./gradlew assembleRelease
```

O APK será gerado em: `app/build/outputs/apk/release/`

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 📞 Suporte

Encontrou algum problema ou tem alguma dúvida?

- 📧 Email: uniforlibrary@unifor.br
- 🐛 Issues: [GitHub Issues](https://github.com/seu-usuario/library-app/issues)
- 💬 Discord: [Servidor da Comunidade](#)

---

## 🙏 Agradecimentos

- [Firebase](https://firebase.google.com/) - Backend as a Service
- [Google Gemini](https://deepmind.google/technologies/gemini/) - IA Generativa
- [Cloudinary](https://cloudinary.com/) - Gerenciamento de mídia
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI Toolkit
- [Material Design 3](https://m3.material.io/) - Design System

---

## 🌟 Star o Projeto

Se este projeto foi útil para você, considere dar uma ⭐️!

---


<p align="center">
  <img src="https://img.shields.io/github/stars/seu-usuario/library-app?style=social" alt="Stars">
  <img src="https://img.shields.io/github/forks/seu-usuario/library-app?style=social" alt="Forks">
  <img src="https://img.shields.io/github/issues/seu-usuario/library-app" alt="Issues">
</p>

