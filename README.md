# 🛍️ Sistema de Gerenciamento - Loja São Judas Tadeu

Projeto de **Trabalho de Conclusão de Curso (TCC)** desenvolvido no curso de **Sistemas de Informação** pela **Universidade Paranaense (UNIPAR)**, com o objetivo de criar uma solução completa para o gerenciamento de uma loja de vestuário — **Loja São Judas Tadeu LTDA** — automatizando processos de **vendas, compras, estoque, finanças e controle de usuários**.

---

## 🧩 Sobre o Projeto

O **Sistema de Gerenciamento - Loja São Judas Tadeu** é uma aplicação web completa voltada à administração de uma varejista de roupas, desenvolvida com foco em **eficiência operacional**, **integridade dos dados** e **usabilidade**.  
A solução foi construída sobre a arquitetura **MVC**, utilizando **JSF + PrimeFaces** na camada de apresentação e **EJB + JPA (Hibernate)** no backend, integrando-se ao banco de dados **PostgreSQL**.

---

## 🛠️ Tecnologias Utilizadas

### 🔹 Backend
- **Java 8 (Jakarta EE)** — Linguagem principal do sistema.  
- **EJB (Enterprise Java Beans)** — Controle de regras de negócio e transações.  
- **JPA / Hibernate** — Persistência de dados com mapeamento objeto-relacional.  
- **GlassFish / Payara** — Servidor de aplicação Java EE para deploy.  

### 🔹 Frontend
- **JSF (JavaServer Faces)** — Framework component-based para a camada de visão.  
- **PrimeFaces 12** — Biblioteca rica de componentes UI e responsividade.  
- **HTML5 / CSS3 / PrimeIcons** — Personalização visual e responsiva da interface.  

### 🔹 Banco de Dados
- **PostgreSQL** — Sistema de gerenciamento de banco de dados robusto e escalável.  

---

## ✨ Funcionalidades Principais

O sistema é modularizado e cobre os principais processos administrativos de uma loja de varejo:

### 🏷️ Gestão de Produtos
- Cadastro completo de produtos e suas derivações (tamanho, cor, marca).  
- Cálculo automático de estoque total.  
- Filtros e busca avançada.  
- Emissão de Relatorios de Produtos em PDF.
- Emissão de Relatorios de Produtos mais Vendidos e Comprados em PDF
- Emissão de Relatorios do geral do estoque em PDF.  

### 📦 Controle de Estoque
- Atualização automática após vendas e compras.  
- Entrada de mercadorias via módulo de compras.  
- Saída de mercadorias via módulo de vendas.  
- Acompanhamento de movimentações.  

### 💰 Módulo de Venda e Compra
- Interface otimizada para o registro, visualização e fechamento de vendas e compras.    
- Integração com contas a receber/pagar e contas bancárias/cofres.
- Emissão de Relatorios com filtros em PDF.
  
### 👥 Gestão de Pessoas
- Controle e cadastro de clientes, fornecedores e funcionários em uma única estrutura.  
- Emissão de Relatorios de Pessoas com filtros em PDF.

### 🧾 Financeiro
- Controle de **Contas a Pagar** e **Contas a Receber**.  
- Controle de **Contas Bancárias** e **Cofres** da empresa.  
- Registro automático de lançamentos gerados por vendas, compras e folha de pagamento.  

### 👔 Módulo de Recursos Humanos (RH)
- Cálculo e registro da **Folha de Pagamento** de cada funcionário, considerando:  
  - Salário base  
  - Horas extras  
  - Adicionais (comissões e bônus)  
  - Descontos (faltas, adiantamentos etc.)  
  - Encargos (INSS, IRRF, FGTS)  
- Geração automática do **salário líquido** e vinculação à competência mensal.  
- Relatórios detalhados de folha de pagamento em PDF, com filtros por período e funcionário.  
- Histórico completo de movimentações salariais de comissão e folhas por mês.  

### 🔐 Gestão de Usuários
- Controle de login e senha criptografada (**BCrypt**).  
- Registro de sessão do usuário logado.  

---

## 🔄 Integração Entre Módulos

O sistema foi projetado com **integração total entre os módulos Financeiro, Vendas, Compras e RH**, garantindo **maior controle dos fluxos financeiros**, **controle de lançamentos** e **coerência entre operações**.

### 💸 Integração Financeira
- Cada **venda** gera automaticamente uma **Conta a Receber**, vinculada ao cliente e método de pagamento.  
- Cada **compra** gera uma **Conta a Pagar**, vinculada ao fornecedor e às parcelas correspondentes.  
- As movimentações de pagamento e recebimento das conta geram lançamentos financeiros e atualizam automaticamente o **saldo de cofres e contas bancárias selecionadas**.  

### 🛒 Integração de Vendas
- O fechamento de uma venda atualiza o estoque e gera automaticamente uma **Conta a Receber**.
- Também é criada uma Movimentação Mensal de Funcionário, registrando a comissão do vendedor responsável pela venda, que posteriormente é incorporada à folha de pagamento do mês. 
- Relatórios de vendas por período, produto, cliente ou funcionário, exportáveis em PDF.  

### 📦 Integração de Compras
- Cada compra fechada atualiza o estoque e gera automaticamente uma **Conta a Pagar**.  
- Relatórios de compras por fornecedor, produto ou período, exportáveis em PDF.  

---

## 💡 Exemplos de Funcionalidades

Abaixo estão alguns exemplos das **funcionalidades práticas** do sistema em execução, demonstrando a integração entre os módulos e a geração de relatórios em tempo real.

---

| DashBoard | Tela de Login |
| :------------------: | :--------------------------------------: |
| ![DashBoard](https://github.com/user-attachments/assets/b222681a-65e6-4819-8af4-19a1e3aec92b) | ![Tela de Login](https://github.com/user-attachments/assets/f35f819e-1f6e-455e-91cf-11c2bc7fd7ef) |

### 🧾 Cadastro e Listagem de Entidades

| Cadastro de Pessoas | Listagem de Pessoa |
| :------------------: | :--------------------------------------: |
| ![Cadastro de Pessoa](https://github.com/user-attachments/assets/fe0d06c4-b6be-4c54-9227-c4e7c7fdadcb) | ![Listagem de Pessoa](https://github.com/user-attachments/assets/8d944a89-824c-4fb9-9162-a9cb1599c185) |

- Interfaces criadas com **PrimeFaces**, utilizando componentes como `p:dataTable`, `p:dialog` e `p:inputText`.  
- Filtros dinâmicos para busca rápida e paginação automática.  
- Validações de campos obrigatórios e feedback visual de sucesso/erro.  
- Edição e exclusão integradas diretamente na tabela, com atualização via **Ajax**.  

---

### 💰 Relatórios Financeiros

| Relatório de Contas a Receber | Relatório de Contas a Pagar |
| :-----------------------------: | :---------------------------: |
| ![Relatório de Contas a Receber](https://github.com/user-attachments/assets/0cc2934e-f81b-42f2-a228-cd67328697e8) | ![Relatório de Contas a Pagar](https://github.com/user-attachments/assets/584e41bf-2306-4334-b309-3da81cfe6d15) |

- Geração de relatórios em **PDF**, com cabeçalhos personalizados e filtros por período, status e tipo de conta.  
- Informações detalhadas de cada conta, incluindo valores, vencimentos e clientes/fornecedores vinculados.  
- Exportação de relatórios diretos do PrimeFaces (`p:commandButton` → `PDFExporter`).  
- Totalizadores automáticos ao final de cada relatório.  

---

### 📈 Contas

| Listagem das contas | Visualização de uma conta |
| :-------------: | :----------------------: |
| ![Listagem de Conta](https://github.com/user-attachments/assets/3079835f-5ecb-4cae-96d2-d4ce0d02136d) | ![Visualização de conta](https://github.com/user-attachments/assets/a53beb90-34d5-45c9-a45a-fc661c5d6a18) |

- Exibição das **entradas e saídas** consolidadas em tempo real.  
- Controle separado por **contas bancárias** e **cofres físicos**.  
- Cálculo instantâneo do **saldo total da empresa**, exibido em destaque no topo da tela.  
- Possibilidade de **filtrar lançamentos** por data, tipo e origem.  

---

### 📊 Relatórios de Desempenho

| Produtos Mais Vendidos | Resumo de Vendas por Período |
| :---------------------: | :---------------------------: |
| ![Produtos Mais Vendidos](https://github.com/user-attachments/assets/5fbf111c-6147-47b9-bb03-215d76c5b3f1) | ![Resumo de Vendas](https://github.com/user-attachments/assets/0c51ccad-c4af-47c1-a2f8-6f83beb3631c) |

- Relatórios gráficos e tabulares com resumo de vendas e compras.  
- Filtros por **período, funcionário e categoria de produto**.  
- Cálculo de **lucro bruto e líquido**.  
- Exportação em PDF e integração direta com o **Dashboard**.  

---

## 🧠 Diagramas UML

- 🔸 [**Diagrama de Caso de Uso**](https://github.com/user-attachments/assets/189e8cdf-88bb-4d1d-ae63-89dbf7578032)  
- 🔸 [**Diagrama de Classes**](https://github.com/user-attachments/assets/9aefc4ef-42ba-4790-bb4e-0c38fa4c60cc)  

### 🔸 Diagramas de Sequência  
*(Abaixo alguns exemplos representativos; o projeto possui diversos outros diagramas que ilustram os principais processos do sistema.)*  

- [Processo de Conta **"ENTRADA"**](https://github.com/user-attachments/assets/1b5b464d-6201-4fa8-9774-162b7c8a46e2)  
- [Processo de Conta **"SAÍDA"**](https://github.com/user-attachments/assets/33cd9035-3cd4-4315-8db0-8c19ffda9734)  
- [Processo de Conta **"TRANSFERÊNCIA"**](https://github.com/user-attachments/assets/bc3566a1-215b-4171-a148-d0206020b9b8)  

---

## 🧾 Estrutura do Projeto (Módulos Principais)

src/<br>
├── Controladores/<br>
│ ├── ProdutoControle.java<br>
│ ├── VendaControle.java<br>
│ ├── ContaControle.java<br>
│ └── ...<br>
├── Entidades/<br>
│ ├── Produto.java<br>
│ ├── ProdutoDerivacao.java<br>
│ ├── Venda.java<br>
│ ├── Pessoa.java<br>
│ ├── Conta.java<br>
│ └── ...<br>
├── Facade/<br>
│ ├── AbstractFacade.java<br>
│ ├── ProdutoFacade.java<br>
│ ├── VendaFacade.java<br>
│ ├── ContaFacade.java<br>
│ ├── PessoaFacade.java<br>
│ └── ...<br>
├── Converters/<br>
│ ├── AbstractConverter.java<br>
│ └── ...

---

## 📚 Metodologia e Arquitetura

O projeto segue o padrão **MVC (Model-View-Controller)**, separando de forma clara as responsabilidades entre:  
- **Model:** Entidades JPA mapeando as tabelas do banco.  
- **View:** Páginas JSF com PrimeFaces e CSS personalizado.  
- **Controller:** ManagedBeans e EJBs intermediando regras de negócio.  

Além disso, foram aplicados princípios de **POO**, **coesão** e **modularidade**, visando fácil manutenção e expansão futura — como a integração de **NFC-e**, **aplicativo mobile** e **relatórios avançados**.

---

## 👨‍💻 Autor

**Felipe Frederico Barros**  
Formado em **Sistemas de Informação – Universidade Paranaense (UNIPAR)**  
Desenvolvedor  

🔗 [LinkedIn](https://www.linkedin.com/in/felipe-frederico-barros1/)  
💻 [GitHub](https://github.com/Felipefredbarros)

---

## 🧾 Licença

Este projeto foi desenvolvido para fins **acadêmicos** e **educacionais**, com possibilidade de expansão para uso **comercial interno** da **Loja São Judas Tadeu LTDA**.
