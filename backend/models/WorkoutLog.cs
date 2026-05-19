using System.ComponentModel.DataAnnotations;

namespace FitTrackBackend.Models;

public class WorkoutLog
{
    [Key]
    public int LogId { get; set; }

    public int WorkoutId { get; set; }

    public int ExerciseId { get; set; }

    public int Sets { get; set; }

    public int Reps { get; set; }

    public decimal Weight { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}