package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BuildingObjectServlet extends BaseServlet {
    private final BuildingObjectService service = new BuildingObjectService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if ("delete".equals(req.getParameter("action"))) {
                service.delete(longParam(req, "id"));
                ok(resp);
            } else {
                json(resp, 200, service.list());
            }
        } catch (Exception e) {
            fail(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            BuildingObject s = new BuildingObject();
            s.setCode(str(req, "code"));
            s.setName(str(req, "name"));
            s.setAddress(str(req, "address"));
            if (hasId(req)) {
                s.setId(longParam(req, "id"));
                service.update(s);
            } else {
                service.create(s);
            }
            ok(resp);
        } catch (Exception e) {
            fail(resp, e);
        }
    }
}
