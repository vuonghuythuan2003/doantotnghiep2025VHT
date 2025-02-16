package ra.doantotnghiep2025.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ra.doantotnghiep2025.security.UserDetailService;
import ra.doantotnghiep2025.service.TokenService;

import java.io.IOException;
@Component
public class JwtAuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private UserDetailService userDetailService;
    @Autowired
    private TokenService tokenService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        try {
            if(token != null && jwtProvider.validateToken(token) && !tokenService.isTokenInvalidated(token)){
                String userName = jwtProvider.getUserNameFromToken(token);
                UserDetails userDetails = userDetailService.loadUserByUsername(userName);
                if (userDetails != null){
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (Exception exception){

        }
        filterChain.doFilter(request,response);
    }
    //Lấy về token gửi lên từ request
    public String getTokenFromRequest(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if(header !=null && header.startsWith("Bearer ")){
            return header.substring(7);
        }
        return null;
    }
}