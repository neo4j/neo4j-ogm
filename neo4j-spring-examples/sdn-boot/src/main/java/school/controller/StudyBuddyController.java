package school.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import school.domain.StudyBuddy;
import school.service.Service;
import school.service.StudyBuddyService;

@RestController
public class StudyBuddyController extends Controller<StudyBuddy> {

    @Autowired
    private StudyBuddyService studdyBuddyService;

    @Override
    public Service<StudyBuddy> getService() {
        return studdyBuddyService;
    }

    @RequestMapping(value = "/studyBuddies", method= RequestMethod.GET)
    public Iterable<StudyBuddy> list(final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        return super.list();
    }

    @RequestMapping(value = "/studyBuddies", method = RequestMethod.POST, consumes = "application/json")
    public  StudyBuddy create (@RequestBody StudyBuddy entity, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        return getService().createOrUpdate(entity);
    }

    @RequestMapping(value="/studyBuddies/{id}", method = RequestMethod.GET)
    public StudyBuddy find(@PathVariable Long id, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        return super.find(id);
    }

    @RequestMapping(value="/studyBuddies/{id}", method = RequestMethod.DELETE)
    public void delete (@PathVariable Long id, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        super.delete(id);
    }

    @RequestMapping(value="/studyBuddies/{id}", method = RequestMethod.PUT, consumes = "application/json")
    public  StudyBuddy update (@PathVariable Long id, @RequestBody StudyBuddy entity, final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        return super.update(id, entity);
    }
}
