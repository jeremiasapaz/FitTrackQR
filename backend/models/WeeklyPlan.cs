using System.ComponentModel.DataAnnotations;

namespace FitTrackBackend.Models;

public class WeeklyPlan
{
    [Key]
    public int PlanId { get; set; }

    public int UserId { get; set; }

    public string DayOfWeek { get; set; } = "";

    public int ExerciseId { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}