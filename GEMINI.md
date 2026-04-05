# Diretrizes para Gemini (AI) - Calculadora Android

Este arquivo contém instruções específicas para que o Gemini (ou outros modelos de IA) forneça as melhores sugestões de código e arquitetura para este projeto.

## 📋 Regras de Ouro
1. **Foco em Jetpack Compose:** Todo código de interface deve utilizar exclusivamente Jetpack Compose com Material Design 3.
2. **Kotlin Idiomático:** Use as melhores práticas do Kotlin (Scope functions, `StateFlow`, `by remember`, etc.).
3. **Padrão Brasileiro:** Mantenha a lógica de formatação de números usando a vírgula `,` como separador decimal.
4. **Responsividade:** Sugira sempre soluções que funcionem bem tanto em modo Retrato (Portrait) quanto Paisagem (Landscape).

## 🏗️ Recomendações Técnicas
- **Gerenciamento de Estado:** Atualmente o projeto usa `rememberSaveable` na `MainActivity`. Quando sugerir melhorias, foque em migrar para `ViewModel` e `StateFlow`.
- **Componentização:** Se um Composable ficar muito grande, sugira a extração para componentes menores e reutilizáveis.
- **Injeção de Dependência:** Para projetos maiores, sugira Hilt, mas para esta calculadora, mantenha a simplicidade a menos que solicitado.
- **Nomenclatura:** Siga o padrão `PascalCase` para Composables e `camelCase` para funções de lógica e variáveis.

## 🧪 Qualidade de Código
- Sempre que criar uma função lógica, sugira um teste unitário correspondente usando JUnit 4/5.
- Evite lógica de negócio pesada dentro de funções `@Composable`.

## 🎨 Design System
- Utilize as cores definidas em `ui.theme.Color.kt`.
- Use `MaterialTheme.typography` para garantir consistência visual.

---
*Este arquivo deve ser lido pela IA no início de cada sessão para garantir conformidade com o projeto.*
