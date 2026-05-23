CREATE DATABASE IF NOT EXISTS student_manage DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE student_manage;

-- 院系表
CREATE TABLE department (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50)   NOT NULL COMMENT '院系名称',
    dean        VARCHAR(20)   NULL     COMMENT '院长',
    phone       VARCHAR(20)   NULL     COMMENT '联系电话',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) COMMENT '院系表';

-- 专业表
CREATE TABLE major (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    name          VARCHAR(50)   NOT NULL COMMENT '专业名称',
    department_id BIGINT        NOT NULL COMMENT '所属院系ID',
    duration      INT           NOT NULL COMMENT '学制(年)',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    PRIMARY KEY (id),
    INDEX idx_department_id (department_id),
    UNIQUE KEY uk_dept_name_deleted (department_id, name, deleted)
) COMMENT '专业表';

-- 班级表
CREATE TABLE class (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(30)   NOT NULL COMMENT '班级名称',
    major_id    BIGINT        NOT NULL COMMENT '所属专业ID',
    grade       INT           NOT NULL COMMENT '年级',
    advisor     VARCHAR(20)   NULL     COMMENT '班主任',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    PRIMARY KEY (id),
    INDEX idx_major_id (major_id),
    INDEX idx_grade (grade),
    UNIQUE KEY uk_major_grade_name_deleted (major_id, grade, name, deleted)
) COMMENT '班级表';

-- 教师表
CREATE TABLE teacher (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    teacher_no    VARCHAR(20)   NOT NULL COMMENT '工号',
    name          VARCHAR(20)   NOT NULL COMMENT '姓名',
    gender        TINYINT       NOT NULL COMMENT '性别: 0女, 1男, 2未知',
    department_id BIGINT        NOT NULL COMMENT '所属院系ID',
    title         VARCHAR(20)   NULL     COMMENT '职称',
    phone         VARCHAR(20)   NULL     COMMENT '联系方式',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_teacher_no (teacher_no),
    INDEX idx_department_id (department_id),
    INDEX idx_name (name)
) COMMENT '教师表';

-- 学生表
CREATE TABLE student (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_no  VARCHAR(20)   NOT NULL COMMENT '学号',
    name        VARCHAR(20)   NOT NULL COMMENT '姓名',
    gender      TINYINT       NOT NULL COMMENT '性别: 0女, 1男, 2未知',
    birth_date  DATE          NULL     COMMENT '出生日期',
    class_id    BIGINT        NOT NULL COMMENT '所属班级ID',
    phone       VARCHAR(20)   NULL     COMMENT '联系方式',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no),
    INDEX idx_class_id (class_id),
    INDEX idx_name (name)
) COMMENT '学生表';

-- 课程表
CREATE TABLE course (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    course_no   VARCHAR(20)   NOT NULL COMMENT '课程号',
    name        VARCHAR(50)   NOT NULL COMMENT '课程名称',
    credit      DECIMAL(3,1)  NOT NULL COMMENT '学分',
    hours       INT           NOT NULL COMMENT '学时',
    teacher_id  BIGINT        NOT NULL COMMENT '授课教师ID',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_no (course_no),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_name (name)
) COMMENT '课程表';

-- 成绩表
CREATE TABLE score (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_id  BIGINT        NOT NULL COMMENT '学生ID',
    course_id   BIGINT        NOT NULL COMMENT '课程ID',
    score       DECIMAL(5,2)  NOT NULL COMMENT '成绩',
    semester    VARCHAR(20)   NOT NULL COMMENT '学期, 如2025-2026-1',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    PRIMARY KEY (id),
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id),
    INDEX idx_semester (semester),
    UNIQUE KEY uk_student_course_semester_deleted (student_id, course_id, semester, deleted)
) COMMENT '成绩表';

-- 系统用户表
CREATE TABLE sys_user (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    username        VARCHAR(30)   NOT NULL COMMENT '用户名',
    password        VARCHAR(100)  NOT NULL COMMENT 'BCrypt加密密码',
    role            VARCHAR(20)   NOT NULL COMMENT '角色: ADMIN/TEACHER/STUDENT',
    ref_id          BIGINT        NULL     COMMENT '关联teacher.id或student.id',
    status          TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 0禁用, 1启用',
    last_login_time DATETIME      NULL     COMMENT '最后登录时间',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    INDEX idx_role (role),
    INDEX idx_ref_id (ref_id)
) COMMENT '系统用户表';

-- 院系数据
INSERT INTO department (name, dean, phone) VALUES
('计算机科学与技术学院', '张明远', '010-62001001'),
('电子信息工程学院', '李建国', '010-62001002'),
('数学与统计学院', '王淑芳', '010-62001003');

-- 专业数据
INSERT INTO major (name, department_id, duration) VALUES
('计算机科学与技术', 1, 4),
('软件工程', 1, 4),
('人工智能', 1, 4),
('电子信息工程', 2, 4),
('通信工程', 2, 4),
('数学与应用数学', 3, 4),
('统计学', 3, 4);

-- 班级数据
INSERT INTO class (name, major_id, grade, advisor) VALUES
('计科2301班', 1, 2023, '刘志强'),
('计科2302班', 1, 2023, '陈晓东'),
('软工2301班', 2, 2023, '赵伟'),
('人工智能2301班', 3, 2023, '孙丽华'),
('电信2301班', 4, 2023, '周国平'),
('通工2301班', 5, 2023, '吴敏'),
('数学2301班', 6, 2023, '黄大年'),
('统计2301班', 7, 2023, '郑晓峰'),
('计科2401班', 1, 2024, '刘志强'),
('软工2401班', 2, 2024, '赵伟');

-- 教师数据
INSERT INTO teacher (teacher_no, name, gender, department_id, title, phone) VALUES
('T2018001', '刘志强', 1, 1, '教授', '13800001001'),
('T2018002', '陈晓东', 1, 1, '副教授', '13800001002'),
('T2019001', '赵伟', 1, 1, '副教授', '13800001003'),
('T2019002', '孙丽华', 0, 1, '讲师', '13800001004'),
('T2020001', '周国平', 1, 2, '教授', '13800002001'),
('T2020002', '吴敏', 0, 2, '副教授', '13800002002'),
('T2021001', '黄大年', 1, 3, '教授', '13800003001'),
('T2021002', '郑晓峰', 1, 3, '副教授', '13800003002');

-- 学生数据
INSERT INTO student (student_no, name, gender, birth_date, class_id, phone) VALUES
('2023001001', '王浩然', 1, '2004-03-15', 1, '13900001001'),
('2023001002', '李思琪', 0, '2004-07-22', 1, '13900001002'),
('2023001003', '张天宇', 1, '2004-11-08', 1, '13900001003'),
('2023002001', '赵雨萱', 0, '2004-05-19', 2, '13900001004'),
('2023002002', '刘嘉豪', 1, '2004-09-03', 2, '13900001005'),
('2023003001', '陈思远', 1, '2004-01-27', 3, '13900001006'),
('2023003002', '杨心怡', 0, '2004-12-11', 3, '13900001007'),
('2023004001', '黄子轩', 1, '2004-06-05', 4, '13900001008'),
('2023004002', '周雅琴', 0, '2004-08-30', 4, '13900001009'),
('2023005001', '吴晨阳', 1, '2004-02-14', 5, '13900001010'),
('2023005002', '林诗涵', 0, '2004-10-25', 5, '13900001011'),
('2023006001', '马天翔', 1, '2004-04-18', 6, '13900001012'),
('2023006002', '何雨桐', 0, '2004-07-09', 6, '13900001013'),
('2023007001', '孙博文', 1, '2004-09-22', 7, '13900001014'),
('2023007002', '徐梦瑶', 0, '2004-11-30', 7, '13900001015'),
('2023008001', '胡宇航', 1, '2004-03-07', 8, '13900001016'),
('2023008002', '郭雅静', 0, '2004-08-16', 8, '13900001017'),
('2024001001', '高俊杰', 1, '2005-02-28', 9, '13900001018'),
('2024001002', '罗欣怡', 0, '2005-06-14', 9, '13900001019'),
('2024002001', '谢泽宇', 1, '2005-04-09', 10, '13900001020');

-- 课程数据
INSERT INTO course (course_no, name, credit, hours, teacher_id) VALUES
('CS101', '数据结构与算法', 4.0, 64, 1),
('CS102', '操作系统', 3.5, 56, 1),
('CS103', '计算机网络', 3.0, 48, 2),
('CS104', '数据库原理', 3.0, 48, 2),
('CS201', '软件工程', 3.0, 48, 3),
('CS202', '机器学习', 3.5, 56, 4),
('EE101', '信号与系统', 4.0, 64, 5),
('EE102', '数字信号处理', 3.0, 48, 5),
('EE103', '通信原理', 3.5, 56, 6),
('MA101', '高等数学', 5.0, 80, 7),
('MA102', '线性代数', 3.0, 48, 7),
('MA103', '概率论与数理统计', 3.0, 48, 8);

-- 成绩数据
INSERT INTO score (student_id, course_id, score, semester) VALUES
(1, 1, 92.00, '2024-2025-1'),
(1, 2, 88.50, '2024-2025-1'),
(1, 3, 85.00, '2024-2025-1'),
(2, 1, 95.50, '2024-2025-1'),
(2, 2, 91.00, '2024-2025-1'),
(2, 4, 89.00, '2024-2025-1'),
(3, 1, 78.00, '2024-2025-1'),
(3, 3, 82.50, '2024-2025-1'),
(4, 1, 88.00, '2024-2025-1'),
(4, 2, 76.50, '2024-2025-1'),
(5, 5, 90.00, '2024-2025-1'),
(5, 6, 85.50, '2024-2025-1'),
(6, 5, 87.00, '2024-2025-1'),
(6, 6, 93.00, '2024-2025-1'),
(7, 7, 91.50, '2024-2025-1'),
(7, 8, 86.00, '2024-2025-1'),
(8, 7, 79.00, '2024-2025-1'),
(9, 10, 94.00, '2024-2025-1'),
(9, 11, 88.50, '2024-2025-1'),
(10, 10, 85.00, '2024-2025-1'),
(10, 12, 82.00, '2024-2025-1');

-- 系统用户数据 (密码均为 123456 的BCrypt加密)
-- 123456 -> $2a$10$Be/wtYYejOnLM4pEyh8v1emieTaisbPjyOqoZkK8ABoCTH0WYhPU.
INSERT INTO sys_user (username, password, role, ref_id, status) VALUES
('admin', '$2a$10$Be/wtYYejOnLM4pEyh8v1emieTaisbPjyOqoZkK8ABoCTH0WYhPU.', 'ADMIN', NULL, 1),
('liuzhiqiang', '$2a$10$Be/wtYYejOnLM4pEyh8v1emieTaisbPjyOqoZkK8ABoCTH0WYhPU.', 'TEACHER', 1, 1),
('chenxiaodong', '$2a$10$Be/wtYYejOnLM4pEyh8v1emieTaisbPjyOqoZkK8ABoCTH0WYhPU.', 'TEACHER', 2, 1),
('wanghaoran', '$2a$10$Be/wtYYejOnLM4pEyh8v1emieTaisbPjyOqoZkK8ABoCTH0WYhPU.', 'STUDENT', 1, 1),
('lisiqi', '$2a$10$Be/wtYYejOnLM4pEyh8v1emieTaisbPjyOqoZkK8ABoCTH0WYhPU.', 'STUDENT', 2, 1);
