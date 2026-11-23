package dio.budgeting.domain;

/**
 * Categorias de gasto. O nome em português é usado nas respostas faladas
 * e ajuda o modelo a escolher a categoria certa.
 */
public enum Category {
    GROCERIES("Mercado"),
    PHARMA("Farmácia"),
    AUTO("Carro"),
    RESTAURANT("Restaurante"),
    TRANSPORT("Transporte"),
    BILLS("Contas da casa"),
    LEISURE("Lazer"),
    EDUCATION("Educação"),
    OTHER("Outros");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
