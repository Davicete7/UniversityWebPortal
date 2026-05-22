package pl.dmcs.david.finalproject.service;

import org.springframework.stereotype.Service;
import pl.dmcs.david.finalproject.model.Degree;
import pl.dmcs.david.finalproject.repository.DegreeRepository;
import java.util.List;

/**
 * Service class for managing Degree business logic.
 */
@Service
public class DegreeService {

    private final DegreeRepository degreeRepository;

    // Constructor Injection
    public DegreeService(DegreeRepository degreeRepository) {
        this.degreeRepository = degreeRepository;
    }

    public List<Degree> getAllDegrees() {
        return degreeRepository.findAll();
    }

    public Degree saveDegree(Degree degree) {
        return degreeRepository.save(degree);
    }
}