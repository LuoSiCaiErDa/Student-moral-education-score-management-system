package com.example.moral;

import com.example.moral.entity.MoralCategory;
import com.example.moral.entity.Student;
import com.example.moral.entity.Teacher;
import com.example.moral.entity.User;
import com.example.moral.repository.MoralCategoryRepository;
import com.example.moral.repository.StudentRepository;
import com.example.moral.repository.TeacherRepository;
import com.example.moral.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final MoralCategoryRepository categoryRepository;

    public DataInitializer(UserRepository userRepository,
                           TeacherRepository teacherRepository,
                           StudentRepository studentRepository,
                           MoralCategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin001");
            admin.setPassword("admin123");
            admin.setRole(com.example.moral.entity.Role.ADMIN);
            admin.setDisplayName("系统管理员");
            userRepository.save(admin);

            User teacherUser = new User();
            teacherUser.setUsername("teacher001");
            teacherUser.setPassword("teacher123");
            teacherUser.setRole(com.example.moral.entity.Role.TEACHER);
            teacherUser.setDisplayName("德育教师");
            userRepository.save(teacherUser);

            User studentUser = new User();
            studentUser.setUsername("student001");
            studentUser.setPassword("student123");
            studentUser.setRole(com.example.moral.entity.Role.STUDENT);
            studentUser.setDisplayName("张三");
            userRepository.save(studentUser);

            User classCadreUser = new User();
            classCadreUser.setUsername("cadre001");
            classCadreUser.setPassword("cadre123");
            classCadreUser.setRole(com.example.moral.entity.Role.CLASS_CADRE);
            classCadreUser.setDisplayName("班干部");
            userRepository.save(classCadreUser);
        }

        if (teacherRepository.count() == 0) {
            Teacher teacher = new Teacher();
            teacher.setJobNumber("T1001");
            teacher.setName("李老师");
            teacher.setEmail("li.teacher@example.com");
            teacher.setPhone("13800000001");
            teacherRepository.save(teacher);
        }

        if (studentRepository.count() == 0) {
            Student student = new Student();
            student.setStudentNumber("student001");
            student.setName("张三");
            student.setGrade("高一");
            student.setClassName("1班");
            student.setTotalScore(0);
            studentRepository.save(student);
        }

        if (categoryRepository.count() == 0) {
            MoralCategory c1 = new MoralCategory();
            c1.setName("仪容仪表");
            c1.setDescription("文明礼仪与仪态表现");
            categoryRepository.save(c1);

            MoralCategory c2 = new MoralCategory();
            c2.setName("志愿服务");
            c2.setDescription("参与志愿服务活动");
            categoryRepository.save(c2);

            MoralCategory c3 = new MoralCategory();
            c3.setName("文明行为");
            c3.setDescription("日常行为规范与遵守纪律");
            categoryRepository.save(c3);
        }
    }
}
