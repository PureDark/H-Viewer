package ml.puredark.hviewer.beans;

public class Selector {
    public String selector, fun, param, regex, replacement;

    public Selector(String selector, String fun, String param, String regex, String replacement) {
        this.selector = selector;
        this.fun = fun;
        this.param = param;
        this.regex = regex;
        this.replacement = replacement;
    }

}
