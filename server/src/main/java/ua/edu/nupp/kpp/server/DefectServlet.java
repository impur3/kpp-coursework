package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefectServlet extends BaseServlet {
    private final DefectService service = new DefectService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String action = req.getParameter("action");
            if ("delete".equals(action)) {
                service.delete(longParam(req, "id"));
                ok(resp);
            } else if ("transition".equals(action)) {
                DefectState to;
                try {
                    to = DefectState.valueOf(str(req, "state"));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Невідомий стан дефекту");
                }
                service.changeState(longParam(req, "id"), to);
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
            long inspectionId = longParam(req, "inspectionId");
            if (hasId(req)) {
                service.update(longParam(req, "id"), inspectionId);
            } else {
                service.create(inspectionId);
            }
            ok(resp);
        } catch (Exception e) {
            fail(resp, e);
        }
    }
}
