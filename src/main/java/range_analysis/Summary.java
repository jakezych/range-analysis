package range_analysis;

public class Summary {
    public Sigma input;
    public Sigma output;

    public Summary(Sigma input, Sigma output) {
        this.input = input;
        this.output = output;
    }

    public Summary() {
        this.input = new Sigma();
        this.output = new Sigma();
    }
}
