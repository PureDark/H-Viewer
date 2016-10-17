package ml.puredark.hviewer.beans;

public class Selector {
    public String selector, path, fun, param, regex, replacement;

    public Selector(){};

    public Selector(String selector, String fun, String param, String regex, String replacement) {
        this.selector = selector;
        this.fun = fun;
        this.param = param;
        this.regex = regex;
        this.replacement = replacement;
    }

    public Selector(String path, String fun, String param, String regex, String replacement, boolean isJson) {
        if(isJson)
            this.path = path;
        else
            this.selector = path;
        this.fun = fun;
        this.param = param;
        this.regex = regex;
        this.replacement = replacement;
    }

    @Override
    public String toString(){
        return "selector="+selector+"\n"+
                "path="+path+"\n"+
                "fun="+fun+"\n"+
                "param="+param+"\n"+
                "regex="+regex+"\n"+
                "replacement="+replacement+"\n";
    }
}
