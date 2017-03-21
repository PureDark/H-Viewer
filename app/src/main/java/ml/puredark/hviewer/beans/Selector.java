package ml.puredark.hviewer.beans;

import java.lang.reflect.Field;

public class Selector {
    public String selector, path, fun, param, regex, replacement;

    public Selector() {
    }

    ;

    public Selector(String selector, String fun, String param, String regex, String replacement) {
        this.selector = selector;
        this.fun = fun;
        this.param = param;
        this.regex = regex;
        this.replacement = replacement;
    }

    public Selector(String path, String fun, String param, String regex, String replacement, boolean isJson) {
        if (isJson)
            this.path = path;
        else
            this.selector = path;
        this.fun = fun;
        this.param = param;
        this.regex = regex;
        this.replacement = replacement;
    }

    @Override
    public String toString() {
        return "selector=" + selector + "\n" +
                "path=" + path + "\n" +
                "fun=" + fun + "\n" +
                "param=" + param + "\n" +
                "regex=" + regex + "\n" +
                "replacement=" + replacement + "\n";
    }

    public void replace(Selector selector) {
        if (selector == null)
            return;
        Field[] fs = Selector.class.getDeclaredFields();
        try {
            for (Field f : fs) {
                f.setAccessible(true);
                if ("path".equals(f.getName())) {
                    String newPath = (String) f.get(selector);
                    if (newPath != null)
                        f.set(this, newPath);
                } else
                    f.set(this, f.get(selector));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
