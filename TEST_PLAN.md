# Plano de Testes - Filadelfia Store

## Objetivo
Este documento descreve um plano de testes baseado nas funcionalidades já implementadas no projeto `filadelfia-store-mobile-spring`.

## Como usar
1. Configure as variáveis de ambiente necessárias.
2. Execute o banco de dados MySQL local ou use o H2 de desenvolvimento.
3. Inicie a aplicação com `mvn spring-boot:run`.
4. Execute os testes automatizados com `mvn test`.

> Observação: hoje o projeto possui apenas um teste de contexto carregado em `src/test/java/com/filadelfia/store/filadelfiastore/FiladelfiastoreApplicationTests.java`.

---

## 1. Pré-requisitos

- `DATABASE_URL` configurado para MySQL ou `H2` para desenvolvimento.
- `DATABASE_USERNAME` e `DATABASE_PASSWORD` definidos.
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `EMAIL_FROM` e `BASE_URL` definidos caso use recursos de email.
- Perfil de aplicação correto, se for usar `application-production.properties`.

## 2. Testes de configuração e inicialização

### 2.1 Validação de ambiente
- Verificar se a aplicação inicia sem erro.
- Verificar se as variáveis de ambiente são lidas corretamente.
- Validar fallback do H2 no `application.properties` quando `DATABASE_URL` não estiver definido.

### 2.2 Validação de contexto Spring
- Executar `mvn test` e confirmar que `contextLoads()` passa.

## 3. Testes de autenticação e autorização

### 3.1 Fluxo de login
- Acessar `/login` e verificar se o formulário de login aparece.
- Tentar login com credenciais válidas e confirmar redirecionamento para dashboard ou página inicial.
- Tentar login com credenciais inválidas e confirmar mensagem de erro.

### 3.2 Logout
- Realizar logout e verificar retorno para a página pública ou de login.

### 3.3 Recuperação de senha
- Acessar a página de esqueci minha senha.
- Enviar email de recuperação com email cadastrado.
- Verificar se o sistema aceita o pedido e envia token de recuperação (pelo menos sem erro de servidor).
- Tentar recuperação com email inexistente e validar mensagem adequada.

### 3.4 Controle de acesso por perfil
- Verificar que ADMIN pode acessar telas de gerenciamento de usuários, categorias, produtos e pedidos.
- Verificar que MANAGER não pode acessar gerenciamento de usuários, mas pode acessar categorias, produtos e pedidos.
- Verificar que USER comum pode acessar carrinho, checkout e histórico de pedidos.
- Verificar que páginas protegidas redirecionam para login quando não autenticado.

## 4. Testes de catálogo de produtos

### 4.1 Lista de produtos
- Verificar que a home page ou página de produtos lista produtos ativos.
- Validar que produtos desativados não são exibidos no catálogo público.

### 4.2 Detalhe de produto
- Abrir a página de detalhe de um produto e confirmar apresentação de informações essenciais: nome, descrição, preço, categoria.
- Verificar botão de adicionar ao carrinho no detalhe do produto.

### 4.3 Pesquisa de produtos
- Testar o fluxo de busca por produto usando a funcionalidade de busca disponível.
- Confirmar que resultados correspondem ao termo pesquisado.

## 5. Testes de carrinho de compras

### 5.1 Adição ao carrinho
- Adicionar produto ao carrinho como usuário autenticado.
- Adicionar produto como visitante sem login.
- Confirmar que o carrinho exibe produto, quantidade e total.

### 5.2 Atualização de carrinho
- Alterar quantidade de item no carrinho.
- Remover item do carrinho.
- Confirmar total é recalculado.

### 5.3 Persistência do carrinho
- Para usuário autenticado, confirmar que o carrinho persiste após logout/login.
- Para visitante, confirmar que o carrinho permanece durante a sessão do navegador.

## 6. Testes de checkout e pedido

### 6.1 Fluxo de checkout
- Iniciar checkout a partir do carrinho.
- Preencher endereço de entrega e dados necessários.
- Confirmar submissão e criação do pedido.

### 6.2 Ordem de pedido
- Confirmar que o pedido é salvo com status inicial correto (ex: `PENDING`).
- Conferir que itens, quantidades e valores do pedido batem com o carrinho.

### 6.3 Histórico de pedidos
- Como usuário, acessar histórico de pedidos e validar lista de pedidos anteriores.
- Como ADMIN/MANAGER, acessar painel de pedidos e verificar listagem.

## 7. Testes de pagamento

### 7.1 Página de pagamento
- Acessar a página de pagamento e confirmar campos disponíveis.
- Verificar que opções de pagamento exibidas correspondem a `PaymentMethod` implementados.

### 7.2 Simulação de sucesso/erro
- Tentar completar um pagamento e verificar comportamento esperado da aplicação.
- Confirmar mensagens de sucesso ou falha exibidas.

## 8. Testes de administração e CRUD

### 8.1 Produtos
- Criar um novo produto (se houver formulário MVC disponível).
- Editar um produto existente.
- Desativar/excluir produto e confirmar que ele não aparece no catálogo público.

### 8.2 Categorias
- Criar nova categoria.
- Editar categoria existente.
- Remover categoria e verificar tratamento de dependência quando produtos usam essa categoria.

### 8.3 Usuários
- Criar usuário ADMIN, MANAGER e USER (se houver UI ou endpoint disponível).
- Alterar papel/role do usuário.
- Confirmar soft delete de usuários (usuário fica inativo, não excluído fisicamente).

## 9. Testes de email e token

### 9.1 Configuração de email
- Verificar que configuração de email é lida de ambiente com `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`.
- Validar envio de email de recuperação de senha em ambiente de teste ou staging.

### 9.2 Token de redefinição de senha
- Gerar token de recuperação de senha e validar seu uso.
- Confirmar que token expirado ou inválido não permite redefinição.

## 10. Testes de APIs e endpoints

### 10.1 Endpoints de API
- Testar `GET /api/v1/products` e `GET /api/v1/categories` para retorno de listagens.
- Testar `GET /api/v1/products/search?q=termo`.
- Testar endpoints de usuários e categorias se estiverem habilitados.

### 10.2 Pesquisa e paginação
- Testar paginação em endpoints que usam `Pageable`.
- Confirmar que página 1 e tamanho customizado funcionam corretamente.

## 11. Testes de segurança e validação

### 11.1 Validação de formulários
- Submeter formulários de cadastro com dados inválidos e confirmar mensagens de erro.
- Testar limites de campos obrigatórios e formatos de email.

### 11.2 Segurança de rotas
- Confirmar que rotas de administração ficam inacessíveis para usuários sem permissão.
- Confirmar que rotas públicas ficam disponíveis para visitantes.

## 12. Testes de regressão e estabilidade

### 12.1 Cenários críticos
- Login/logout.
- Adição de item ao carrinho e checkout.
- Criação e edição de produto/categoria.
- Recuperação de senha.

### 12.2 Comportamento esperado após alterações
- Sempre validar que `Dashboard`, `Cart`, `Checkout` e `Orders` carregam sem exceção.
- Conferir logs do servidor para erros 500 durante o uso manual.

## 13. Sugestões de teste automatizado

- Criar testes unitários para serviços: `UserServiceImpl`, `ProductServiceImpl`, `OrderServiceImpl`, `CartServiceImpl`.
- Criar testes de integração para controladores MVC usando `MockMvc`.
- Criar testes de repositório com `@DataJpaTest` para `UserRepository`, `ProductRepository`, `OrderRepository`.
- Automatizar validações de segurança usando `@WithMockUser`.

## 14. Observações finais

- O projeto já possui suporte a Spring Boot Test via `spring-boot-starter-test`.
- Hoje há apenas um teste de carga do contexto (`contextLoads`).
- Este plano é focado nas funcionalidades já implementadas e nos pontos de maior risco identificados pelo código.

---

### Comando para rodar testes
```bash
mvn test
```
