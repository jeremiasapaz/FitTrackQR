namespace FitTrackBackend.DTOs;

public class CreateWeeklyPlanRequest
{
    public int UserId { get; set; }

    public string DayOfWeek { get; set; } = "";

    public List<int> ExerciseIds { get; set; } = new();
}