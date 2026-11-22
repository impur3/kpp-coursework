package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RepairTaskServlet extends BaseServlet {
    private final RepairTaskService service = new RepairTaskService();

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
            RepairTask t = new RepairTask();
            t.setDefectId(longParam(req, "defectId"));
            t.setDescription(str(req, "description"));
            t.setPlannedDate(str(req, "plannedDate"));
            if (hasId(req)) {
                t.setId(longParam(req, "id"));
                service.update(t);
            } else {
                service.create(t);
            }
            ok(resp);
        } catch (Exception e) {
            fail(resp, e);
        }
    }
}
