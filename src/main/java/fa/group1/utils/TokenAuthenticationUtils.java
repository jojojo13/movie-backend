package fa.group1.utils;

import static java.util.Collections.emptyList;

import fa.group1.exceptions.ResourceNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import io.jsonwebtoken.Jwts;



public class TokenAuthenticationUtils {
    //    static final long EXPIRATIONTIME = 864_000_000; // 10 days
    static final String SECRET = "group2";
    static final String TOKEN_PREFIX = "Bearer";
    static final String HEADER_STRING = "Authorization";

    public static Authentication getAuthentication(String token) {

        String user = null;
        if (token != null) {
            try {
                user = Jwts.parser()
                        .setSigningKey(SECRET)
                        .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                        .getBody()
                        .getSubject();

            } catch (Exception e) {
                throw new ResourceNotFoundException("User not found");
            }

            return user != null ?
                    new UsernamePasswordAuthenticationToken(user, null, emptyList()) :
                    null;
        }
        return null;
    }
}
