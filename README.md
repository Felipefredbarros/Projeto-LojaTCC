🛍️ Sistema de Gerenciamento - Loja São Judas Tadeu

Projeto de Trabalho de Conclusão de Curso (TCC) desenvolvido no curso de Sistemas de Informação pela Universidade Paranaense (UNIPAR), com o objetivo de criar uma solução completa para o gerenciamento de uma loja de vestuário — Loja São Judas Tadeu LTDA — automatizando processos de vendas, compras, estoque, finanças e controle de usuários.

--

🧩 Sobre o Projeto

O Sistema de Gerenciamento - Loja São Judas Tadeu é uma aplicação web completa voltada à administração de uma varejista de roupas, desenvolvida com foco em eficiência operacional, integridade dos dados e usabilidade.<br>
A solução foi construída sobre a arquitetura MVC, utilizando JSF + PrimeFaces na camada de apresentação e EJB + JPA (Hibernate) no backend, integrando-se ao banco de dados PostgreSQL.

--

🛠️ Tecnologias Utilizadas
🔹 Backend
-Java 8 (Jakarta EE) — Linguagem principal do sistema.<br>
-EJB (Enterprise Java Beans) — Controle de regras de negócio e transações.<br>
-JPA / Hibernate — Persistência de dados com mapeamento objeto-relacional.<br>
-GlassFish / Payara — Servidor de aplicação Java EE para deploy.<br>

🔹 Frontend
-JSF (JavaServer Faces) — Framework component-based para a camada de visão.<br>
-PrimeFaces 12 — Biblioteca rica de componentes UI e responsividade.<br>
-HTML5 / CSS3 / PrimeIcons — Personalização visual e responsiva da interface.

🔹 Banco de Dados
-PostgreSQL — Sistema de gerenciamento de banco de dados robusto e escalável.

--

✨ Funcionalidades Principais
O sistema é modularizado e cobre os principais processos administrativos de uma loja de varejo:

🏷️ Gestão de Produtos
-Cadastro completo de produtos e suas derivações (tamanho, cor, marca).<br>
-Cálculo automático de estoque total.<br>
-Filtros e busca avançada.<br>
-Relatórios de produtos com estoque baixo.

📦 Controle de Estoque
-Atualização automática após vendas e compras.<br>
-Entrada de mercadorias via módulo de compras.<br>
-Acompanhamento de movimentações.

💰 Módulo de Vendas (PDV)
-Interface otimizada para vendas no balcão.<br>
-Cálculo de totais, descontos e troco.<br>
-Integração com contas a receber e fechamento de caixa.

👥 Gestão de Pessoas
-Controle de clientes, fornecedores e funcionários em uma única estrutura.<br>
-Histórico de compras e vendas vinculadas.

🧾 Financeiro
-Controle de Contas a Pagar e Contas a Receber.<br>
-Fluxo de Caixa e relatórios financeiros.<br>
-Emissão de relatórios em PDF.

🔐 Gestão de Usuários e Acesso
-Controle de login e senha criptografada (BCrypt).<br>
-Níveis de acesso (Administrador / Vendedor).<br>
-Registro de sessão do usuário logado.

--

📊 Dashboard e Relatórios
O painel principal (Dashboard) reúne indicadores e métricas em tempo real:<br>
-Total de produtos cadastrados<br>
-Valor total de vendas e compras<br>
-Lucro líquido<br>
-Produtos com estoque crítico<br>
-Contas a pagar e receber próximas do vencimento<br>

Todos os relatórios podem ser exportados em PDF, facilitando a tomada de decisão gerencial.

--

|                     Tela de Login                    |                  Dashboard Principal                 |
| :--------------------------------------------------: | :--------------------------------------------------: |
| ![Tela de Login](\[COLE_A_URL_DA_IMAGEM_LOGIN_AQUI]) | ![Dashboard](\[COLE_A_URL_DA_IMAGEM_DASHBOARD_AQUI]) |
|                      Cadastro de Produto                     |           Ponto de Venda (PDV)           |
| :----------------------------------------------------------: | :--------------------------------------: |
| ![Cadastro de Produto](\[COLE_A_URL_DA_IMAGEM_PRODUTO_AQUI]) | ![PDV](\[COLE_A_URL_DA_IMAGEM_PDV_AQUI]) |

--

🧠 Diagramas UML
🔸 Diagrama de Casos de Uso<br>
🔸 Diagrama de Classes<br>
🔸 Diagrama de Sequência (Exemplo: Processo de Venda)

--

🧾 Estrutura do Projeto (Módulos Principais)<br>
src/
├── Controladores/
│   ├── ProdutoControle.java
│   ├── VendaControle.java
│   ├── ContaControle.java
│   └── ...
├── Entidades/
│   ├── Produto.java
│   ├── ProdutoDerivacao.java
│   ├── Venda.java
│   ├── Pessoa.java
│   ├── Conta.java
│   └── ...
├── Facade/
│   ├── AbstractFacade.java
│   ├── ProdutoFacade.java
│   ├── VendaFacade.java
│   ├── ContaFacade.java
│   └── PessoaFacade.java
│   └── ...
├── Converters/
│   ├── AbstractConverter.java

--

📚 Metodologia e Arquitetura
O projeto segue o padrão MVC (Model-View-Controller), separando de forma clara as responsabilidades entre:
-Model: Entidades JPA mapeando as tabelas do banco.<br>
-View: Páginas JSF com PrimeFaces e CSS personalizado.<br>
-Controller: ManagedBeans e EJBs intermediando regras de negócio.

Além disso, foram aplicados princípios de POO, coesão e modularidade, visando fácil manutenção e expansão futura — como a integração de NFC-e, mobile app e relatórios avançados.

--

👨‍💻 Autor
Felipe Frederico Barros<br>
Acadêmico de Sistemas de Informação – Universidade Paranaense (UNIPAR)<br>
Desenvolvedor Full Stack (Java / JSF / PrimeFaces / Spring / Angular)

🔗 LinkedIn

💻 GitHub

--

🧾 Licença
Este projeto foi desenvolvido para fins acadêmicos e educacionais, com possibilidade de expansão para uso comercial interno da Loja São Judas Tadeu LTDA.
