INSERT INTO users (username, password, role, display_name) VALUES ('admin001', 'admin123', 'ADMIN', '系统管理员');
INSERT INTO users (username, password, role, display_name) VALUES ('teacher001', 'teacher123', 'TEACHER', '德育教师');
INSERT INTO users (username, password, role, display_name) VALUES ('student001', 'student123', 'STUDENT', '张三');
INSERT INTO users (username, password, role, display_name) VALUES ('cadre001', 'cadre123', 'CLASS_CADRE', '班干部');

INSERT INTO teachers (job_number, name, email, phone) VALUES ('T1001', '李老师', 'li.teacher@example.com', '13800000001');

INSERT INTO students (student_number, name, grade, class_name, total_score) VALUES ('student001', '张三', '高一', '1班', 0);

INSERT INTO moral_categories (name, description) VALUES ('仪容仪表', '文明礼仪与仪态表现');
INSERT INTO moral_categories (name, description) VALUES ('志愿服务', '参与志愿服务活动');
INSERT INTO moral_categories (name, description) VALUES ('文明行为', '日常行为规范与遵守纪律');
