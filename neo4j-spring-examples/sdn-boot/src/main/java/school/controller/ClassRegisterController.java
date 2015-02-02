package school.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import school.domain.Course;
import school.service.ClassRegisterService;
import school.service.Service;

import javax.servlet.http.HttpServletResponse;

@RestController
public class ClassRegisterController extends Controller<Course> {

    @Autowired
    private ClassRegisterService classRegisterService;

    @RequestMapping(value = "/classes", method= RequestMethod.GET)
    public Iterable<Course> list(final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        return super.list();
    }

    @RequestMapping(value = "/classes", method = RequestMethod.POST, consumes = "application/json")
    public Course create (@RequestBody Course entity, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        return super.create(entity);
    }

    @RequestMapping(value="/classes/{id}", method = RequestMethod.GET)
    public Course find(@PathVariable Long id, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        return super.find(id);
    }

    @RequestMapping(value="/classes/{id}", method = RequestMethod.DELETE)
    public void delete (@PathVariable Long id, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        super.delete(id);
    }

    @RequestMapping(value="/classes/{id}", method = RequestMethod.PUT, consumes = "application/json")
    public Course update (@PathVariable Long id, @RequestBody Course entity, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        return super.update(id, entity);
    }

    @Override
    public Service<Course> getService() {
        return classRegisterService;
    }


}
