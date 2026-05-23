package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.common.constants.RoleConstants;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.common.util.SecurityUtils;
import com.example.backend.dto.request.ChangePasswordRequest;
import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.response.*;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.example.backend.security.JwtTokenProvider;
import com.example.backend.security.LoginUser;
import com.example.backend.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final CourseMapper courseMapper;
    private final DepartmentMapper departmentMapper;
    private final MajorMapper majorMapper;
    private final ClazzMapper clazzMapper;
    private final ScoreMapper scoreMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(SysUserMapper sysUserMapper, StudentMapper studentMapper,
                           TeacherMapper teacherMapper, CourseMapper courseMapper,
                           DepartmentMapper departmentMapper, MajorMapper majorMapper,
                           ClazzMapper clazzMapper, ScoreMapper scoreMapper,
                           JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.courseMapper = courseMapper;
        this.departmentMapper = departmentMapper;
        this.majorMapper = majorMapper;
        this.clazzMapper = clazzMapper;
        this.scoreMapper = scoreMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginVO login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已禁用");
        }
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setRefId(user.getRefId());
        return vo;
    }

    @Override
    public UserInfoVO getCurrentUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        UserInfoVO vo = new UserInfoVO();
        vo.setId(loginUser.getId());
        vo.setUsername(loginUser.getUsername());
        vo.setRole(loginUser.getRole());
        vo.setRefId(loginUser.getRefId());

        SysUser sysUser = sysUserMapper.selectById(loginUser.getId());
        if (sysUser != null && sysUser.getLastLoginTime() != null) {
            vo.setLastLoginTime(sysUser.getLastLoginTime().toString());
        }

        if (RoleConstants.ADMIN.equals(loginUser.getRole())) {
            vo.setName("管理员");
        } else if (RoleConstants.TEACHER.equals(loginUser.getRole()) && loginUser.getRefId() != null) {
            Teacher teacher = teacherMapper.selectById(loginUser.getRefId());
            if (teacher != null) {
                vo.setName(teacher.getName());
                vo.setPhone(teacher.getPhone());
                vo.setGender(teacher.getGender());
                vo.setTeacherNo(teacher.getTeacherNo());
                vo.setTitle(teacher.getTitle());
                if (teacher.getDepartmentId() != null) {
                    Department dept = departmentMapper.selectById(teacher.getDepartmentId());
                    if (dept != null) vo.setDepartmentName(dept.getName());
                }
            }
        } else if (RoleConstants.STUDENT.equals(loginUser.getRole()) && loginUser.getRefId() != null) {
            Student student = studentMapper.selectById(loginUser.getRefId());
            if (student != null) {
                vo.setName(student.getName());
                vo.setPhone(student.getPhone());
                vo.setGender(student.getGender());
                vo.setStudentNo(student.getStudentNo());
                if (student.getClassId() != null) {
                    Clazz clazz = clazzMapper.selectById(student.getClassId());
                    if (clazz != null) {
                        vo.setClassName(clazz.getName());
                        if (clazz.getMajorId() != null) {
                            Major major = majorMapper.selectById(clazz.getMajorId());
                            if (major != null && major.getDepartmentId() != null) {
                                Department dept = departmentMapper.selectById(major.getDepartmentId());
                                if (dept != null) vo.setDepartmentName(dept.getName());
                            }
                        }
                    }
                }
            }
        }
        return vo;
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = sysUserMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    @Override
    public DashboardOverviewVO getDashboardOverview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();
        LoginUser loginUser = SecurityUtils.getLoginUser();

        if (RoleConstants.ADMIN.equals(loginUser.getRole())) {
            vo.setStudentCount(studentMapper.selectCount(new LambdaQueryWrapper<>()));
            vo.setTeacherCount(teacherMapper.selectCount(new LambdaQueryWrapper<>()));
            vo.setCourseCount(courseMapper.selectCount(new LambdaQueryWrapper<>()));
            vo.setDepartmentCount(departmentMapper.selectCount(new LambdaQueryWrapper<>()));
        } else if (RoleConstants.TEACHER.equals(loginUser.getRole())) {
            Long teacherId = loginUser.getRefId();
            List<Long> courseIds = getTeacherCourseIds(teacherId);
            Teacher teacher = teacherId == null ? null : teacherMapper.selectById(teacherId);
            vo.setCourseCount((long) courseIds.size());
            vo.setStudentCount(countStudentsByCourseIds(courseIds));
            vo.setTeacherCount(teacher == null ? 0L : 1L);
            vo.setDepartmentCount(teacher != null && teacher.getDepartmentId() != null ? 1L : 0L);
        } else {
            Long studentId = loginUser.getRefId();
            Student student = studentId == null ? null : studentMapper.selectById(studentId);
            List<Long> courseIds = getStudentCourseIds(studentId);
            vo.setStudentCount(student == null ? 0L : 1L);
            vo.setCourseCount((long) courseIds.size());
            vo.setTeacherCount(countTeachersByCourseIds(courseIds));
            vo.setDepartmentCount(resolveDepartmentId(student) == null ? 0L : 1L);
        }
        return vo;
    }

    @Override
    public List<DepartmentStudentCountVO> getDepartmentStudentCount() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (RoleConstants.TEACHER.equals(loginUser.getRole())) {
            Teacher teacher = loginUser.getRefId() == null ? null : teacherMapper.selectById(loginUser.getRefId());
            Long departmentId = teacher == null ? null : teacher.getDepartmentId();
            return buildScopedDepartmentStudentCounts(departmentId);
        }
        if (RoleConstants.STUDENT.equals(loginUser.getRole())) {
            Student student = loginUser.getRefId() == null ? null : studentMapper.selectById(loginUser.getRefId());
            return buildScopedDepartmentStudentCounts(resolveDepartmentId(student));
        }

        List<Department> departments = departmentMapper.selectList(new LambdaQueryWrapper<>());
        List<DepartmentStudentCountVO> result = new ArrayList<>();

        for (Department dept : departments) {
            result.add(buildDepartmentStudentCount(dept));
        }
        return result;
    }

    @Override
    public List<ScoreDistributionVO> getScoreDistribution(String semester) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        if (semester != null && !semester.isBlank()) {
            wrapper.eq(Score::getSemester, semester);
        }
        applyScoreScope(wrapper);
        List<Score> scores = scoreMapper.selectList(wrapper);

        String[][] ranges = {{"0-59", "0", "59"}, {"60-69", "60", "69"}, {"70-79", "70", "79"}, {"80-89", "80", "89"}, {"90-100", "90", "100"}};
        List<ScoreDistributionVO> result = new ArrayList<>();
        for (String[] range : ranges) {
            BigDecimal low = new BigDecimal(range[1]);
            BigDecimal high = new BigDecimal(range[2]);
            long count = scores.stream()
                    .filter(s -> s.getScore().compareTo(low) >= 0 && s.getScore().compareTo(high) <= 0)
                    .count();
            ScoreDistributionVO vo = new ScoreDistributionVO();
            vo.setRange(range[0]);
            vo.setCount(count);
            result.add(vo);
        }
        return result;
    }

    private void applyScoreScope(LambdaQueryWrapper<Score> wrapper) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (RoleConstants.ADMIN.equals(loginUser.getRole())) {
            return;
        }
        if (RoleConstants.STUDENT.equals(loginUser.getRole())) {
            if (loginUser.getRefId() == null) {
                wrapper.eq(Score::getId, -1L);
                return;
            }
            wrapper.eq(Score::getStudentId, loginUser.getRefId());
            return;
        }
        if (loginUser.getRefId() == null) {
            wrapper.eq(Score::getId, -1L);
            return;
        }
        List<Long> courseIds = getTeacherCourseIds(loginUser.getRefId());
        if (courseIds.isEmpty()) {
            wrapper.eq(Score::getId, -1L);
            return;
        }
        wrapper.in(Score::getCourseId, courseIds);
    }

    private List<DepartmentStudentCountVO> buildScopedDepartmentStudentCounts(Long departmentId) {
        if (departmentId == null) {
            return List.of();
        }
        Department department = departmentMapper.selectById(departmentId);
        if (department == null) {
            return List.of();
        }
        return List.of(buildDepartmentStudentCount(department));
    }

    private DepartmentStudentCountVO buildDepartmentStudentCount(Department department) {
        List<Major> majors = majorMapper.selectList(
                new LambdaQueryWrapper<Major>().eq(Major::getDepartmentId, department.getId()));
        List<Long> majorIds = majors.stream().map(Major::getId).collect(Collectors.toList());

        long count = 0;
        if (!majorIds.isEmpty()) {
            List<Clazz> clazzes = clazzMapper.selectList(
                    new LambdaQueryWrapper<Clazz>().in(Clazz::getMajorId, majorIds));
            List<Long> clazzIds = clazzes.stream().map(Clazz::getId).collect(Collectors.toList());
            if (!clazzIds.isEmpty()) {
                count = studentMapper.selectCount(
                        new LambdaQueryWrapper<Student>().in(Student::getClassId, clazzIds));
            }
        }

        DepartmentStudentCountVO vo = new DepartmentStudentCountVO();
        vo.setDepartmentName(department.getName());
        vo.setStudentCount(count);
        return vo;
    }

    private List<Long> getTeacherCourseIds(Long teacherId) {
        if (teacherId == null) {
            return List.of();
        }
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                        .eq(Course::getTeacherId, teacherId))
                .stream()
                .map(Course::getId)
                .collect(Collectors.toList());
    }

    private List<Long> getStudentCourseIds(Long studentId) {
        if (studentId == null) {
            return List.of();
        }
        return scoreMapper.selectList(new LambdaQueryWrapper<Score>()
                        .eq(Score::getStudentId, studentId))
                .stream()
                .map(Score::getCourseId)
                .distinct()
                .collect(Collectors.toList());
    }

    private long countStudentsByCourseIds(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return 0L;
        }
        return scoreMapper.selectList(new LambdaQueryWrapper<Score>().in(Score::getCourseId, courseIds))
                .stream()
                .map(Score::getStudentId)
                .distinct()
                .count();
    }

    private long countTeachersByCourseIds(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return 0L;
        }
        return courseMapper.selectList(new LambdaQueryWrapper<Course>().in(Course::getId, courseIds))
                .stream()
                .map(Course::getTeacherId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();
    }

    private Long resolveDepartmentId(Student student) {
        if (student == null || student.getClassId() == null) {
            return null;
        }
        Clazz clazz = clazzMapper.selectById(student.getClassId());
        if (clazz == null || clazz.getMajorId() == null) {
            return null;
        }
        Major major = majorMapper.selectById(clazz.getMajorId());
        return major == null ? null : major.getDepartmentId();
    }
}
