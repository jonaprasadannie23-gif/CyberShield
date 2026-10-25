package ui;

import repository.IncidentRepository;
import repository.TaskRepository;
import repository.UserRepository;
import service.*;

/**
 * Application-wide service locator / dependency container.
 * Instantiates all repositories and services once; provides them to UI frames.
 *
 * This is a simplified Singleton for a college-level project.
 * In production code, use a proper DI framework.
 */
public class AppContext {

    private static AppContext instance;

    // Repositories
    private final UserRepository userRepository;
    private final IncidentRepository incidentRepository;
    private final TaskRepository taskRepository;

    // Services
    private final AuthenticationService authService;
    private final RiskAssessmentService riskAssessmentService;
    private final IncidentService incidentService;
    private final TaskService taskService;
    private final ReportService reportService;
    private final PasswordStrengthService passwordStrengthService;

    private AppContext() {
        userRepository     = new UserRepository();
        incidentRepository = new IncidentRepository();
        taskRepository     = new TaskRepository();

        authService            = new AuthenticationService(userRepository);
        riskAssessmentService  = new RiskAssessmentService();
        incidentService        = new IncidentService(incidentRepository, riskAssessmentService);
        taskService            = new TaskService(taskRepository);
        reportService          = new ReportService(incidentService, taskService);
        passwordStrengthService = new PasswordStrengthService();
    }

    public static AppContext getInstance() {
        if (instance == null) instance = new AppContext();
        return instance;
    }

    // --- Accessors ---

    public AuthenticationService getAuthService()          { return authService; }
    public IncidentService getIncidentService()            { return incidentService; }
    public TaskService getTaskService()                    { return taskService; }
    public ReportService getReportService()                { return reportService; }
    public PasswordStrengthService getPasswordStrengthService() { return passwordStrengthService; }

    public LoginFrame createLoginFrame() {
        return new LoginFrame(authService);
    }
}
