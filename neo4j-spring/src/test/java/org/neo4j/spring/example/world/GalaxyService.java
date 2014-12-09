package org.neo4j.spring.example.world;

import org.neo4j.spring.domain.World;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class GalaxyService {


    // TODO: private WorldRepository extends GraphRepository<World>

    @Autowired
    //private GraphRepository<World> worldRepository;

    private WorldRepository worldRepository;

    public long getNumberOfWorlds() {
        return worldRepository.count();
    }

    public World createWorld(String name, int moons) {
        return worldRepository.save(new World(name, moons));
    }

    public Iterable<World> getAllWorlds() {
        return worldRepository.findAll();
    }

    public World findWorldById(Long id) {
        return worldRepository.findOne(id);
    }

    // This is using the schema based index
    public World findWorldByName(String name) {
        //return worldRepository.findBySchemaPropertyValue("name", name);
        Iterable<World> worlds = worldRepository.findByProperty("name", name);
        if (worlds.iterator().hasNext()) {
            return worlds.iterator().next();
        } else {
            return null;
        }
    }

    // This is using the legacy index
    public Iterable<World> findAllByNumberOfMoons(int numberOfMoons) {
        //return worldRepository.findAllByPropertyValue("moons", numberOfMoons);
        return worldRepository.findByProperty("moons", numberOfMoons);
    }

    public Collection<World> makeSomeWorlds() {

        Collection<World> worlds = new ArrayList<World>();

        // Solar worlds
        worlds.add(createWorld("Mercury", 0));
        worlds.add(createWorld("Venus", 0));

        World earth = createWorld("Earth", 1);
        World mars = createWorld("Mars", 2);



        mars.addRocketRouteTo(earth);

        // todo: handle bi-directional automatically
        earth.addRocketRouteTo(mars);


        // this is a bit silly
        worldRepository.save(mars);

        // todo: handle-bidirectional automatically
        worldRepository.save(earth);

        worlds.add(earth);
        worlds.add(mars);

        worlds.add(createWorld("Jupiter", 63));
        worlds.add(createWorld("Saturn", 62));
        worlds.add(createWorld("Uranus", 27));
        worlds.add(createWorld("Neptune", 13));

        // Norse worlds
        worlds.add(createWorld("Alfheimr", 0));
        worlds.add(createWorld("Midgard", 1));
        worlds.add(createWorld("Muspellheim", 2));
        worlds.add(createWorld("Asgard", 63));
        worlds.add(createWorld("Hel", 62));

        return worlds;
    }

    public void deleteAll() {
        worldRepository.deleteAll();
    }



}
