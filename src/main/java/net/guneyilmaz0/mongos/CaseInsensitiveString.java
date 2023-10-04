package net.guneyilmaz0.mongos;

import lombok.AllArgsConstructor;
import java.util.regex.Pattern;

@AllArgsConstructor
public class CaseInsensitiveString {
    private String string;

    public Pattern compile() {
        return Pattern.compile(string, Pattern.CASE_INSENSITIVE);
    }
}
