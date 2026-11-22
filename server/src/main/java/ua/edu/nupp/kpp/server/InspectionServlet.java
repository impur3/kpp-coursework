package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class InspectionServlet extends BaseServlet {
    private final InspectionService service = new InspectionService();

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
            Inspection m = new Inspection();
            m.setObjectId(longParam(req, "objectId"));
            m.setDefectType(str(req, "defectType"));
            m.setMeasuredValue(doubleParam(req, "measuredValue"));
            m.setAllowedValue(doubleParam(req, "allowedValue"));
            m.setScaleScore(doubleParam(req, "scaleScore"));
            m.setSafetyScore(doubleParam(req, "safetyScore"));
            if (hasId(req)) {
                m.setId(longParam(req, "id"));
                service.update(m);
            } else {
                service.create(m);
            }
            ok(resp);
        } catch (Exception e) {
            fail(resp, e);
        }
    }
}
