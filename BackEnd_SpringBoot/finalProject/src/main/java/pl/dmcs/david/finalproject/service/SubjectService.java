package pl.dmcs.david.finalproject.service;

import org.springframework.stereotype.Service;
import pl.dmcs.david.finalproject.model.Subject;
import pl.dmcs.david.finalproject.repository.SubjectRepository;
import java.util.List;

/**
 * Service class for managing subjects.
 */
@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public Subject saveSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public List<Subject> getSubjectsByTeacher(Long teacherId) {
        return subjectRepository.findByAssignedTeacherId(teacherId);
    }
}