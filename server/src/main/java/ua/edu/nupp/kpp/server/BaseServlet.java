package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/** Спільні допоміжні методи для всіх Servlet. */
public abstract class BaseServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(BaseServlet.class.getName());
    protected static final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        super.service(req, resp);
    }

    protected void json(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getWriter(), data);
    }

    protected void ok(HttpServletResponse resp) throws IOException {
        json(resp, 200, Map.of("status", "ok"));
    }

    /** HTTP 400 і JSON-об'єкт із полем error. */
    protected void fail(HttpServletResponse resp, Exception e) throws IOException {
        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        LOG.warning("Помилка запиту: " + message);
        json(resp, 400, Map.of("error", message));
    }

    protected String str(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    protected long longParam(HttpServletRequest req, String name) {
        String v = str(req, name);
        if (v.isEmpty()) {
            throw new IllegalArgumentException("Не вказано параметр " + name);
        }
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некоректне ціле число у параметрі " + name);
        }
    }

    protected double doubleParam(HttpServletRequest req, String name) {
        String v = str(req, name).replace(',', '.');
        if (v.isEmpty()) {
            throw new IllegalArgumentException("Не вказано параметр " + name);
        }
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некоректне число у параметрі " + name);
        }
    }

    /** true, якщо у запиті є непорожній id (редагування), інакше — створення. */
    protected boolean hasId(HttpServletRequest req) {
        String v = str(req, "id");
        return !v.isEmpty() && !"0".equals(v);
    }
}
