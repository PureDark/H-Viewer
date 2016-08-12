package ml.puredark.hviewer.beans;

public class Selector {
    public String selector, fun, param, regex;

    public Selector(String selector, String fun, String param, String regex) {
        this.selector = selector;
        this.fun = fun;
        this.param = param;
        this.regex = regex;
    }

}
