package school.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import school.domain.ClassRegister;
import school.service.ClassRegisterService;
import school.service.Service;

import javax.servlet.http.HttpServletResponse;

@RestController
public class ClassRegisterController extends Controller<ClassRegister> {

    @Autowired
    private ClassRegisterService classRegisterService;

    @RequestMapping(value = "/classes", method= RequestMethod.GET)
    public Iterable<ClassRegister> list(final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        return super.list();
    }

    @RequestMapping(value = "/classes", method = RequestMethod.POST, consumes = "application/json")
    public  ClassRegister create (@RequestBody ClassRegister entity, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        return super.create(entity);
    }

    @RequestMapping(value="/classes/{id}", method = RequestMethod.GET)
    public ClassRegister find(@PathVariable Long id, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        return super.find(id);
    }

    @RequestMapping(value="/classes/{id}", method = RequestMethod.DELETE)
    public void delete (@PathVariable Long id, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        super.delete(id);
    }

    @RequestMapping(value="/classes/{id}", method = RequestMethod.PUT, consumes = "application/json")
    public  ClassRegister update (@PathVariable Long id, @RequestBody ClassRegister entity, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        return super.update(id, entity);
    }

    @Override
    public Service<ClassRegister> getService() {
        return classRegisterService;
    }


}
