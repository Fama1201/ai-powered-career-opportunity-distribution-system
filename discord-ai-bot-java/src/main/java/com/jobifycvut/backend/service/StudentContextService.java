package com.jobifycvut.backend.service;

import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.model.User;
import com.jobifycvut.backend.repository.StudentRepository;
import com.jobifycvut.backend.repository.UserRepository;
import com.jobifycvut.backend.security.SecurityUtil;
import org.springframework.stereotype.Service;

@Service
public class StudentContextService {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public StudentContextService(StudentRepository studentRepository, UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    public StudentEntity getOrCreateCurrentStudent(){
        Long userId=SecurityUtil.requireUserId();
        String emailFromJwt=SecurityUtil.getPrincipal() !=
                null?SecurityUtil.getPrincipal().getEmail() : null;

        var byId=studentRepository.findById(userId);
        if(byId.isPresent()){
            return byId.get();
        }

        if (emailFromJwt != null && !emailFromJwt.isBlank()) {
            var byEmail = studentRepository.findByEmail(emailFromJwt);
            if (byEmail.isPresent()) {
                return byEmail.get();}
        }

        User user=userRepository.findById(userId).orElse(null);

        StudentEntity student=new StudentEntity();
        if(user!=null){
            student.setEmail(emailFromJwt);
            student.setName("Student");
        }
        return studentRepository.save(student);
    }
}
