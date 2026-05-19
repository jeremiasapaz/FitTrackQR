using System.ComponentModel.DataAnnotations;

namespace FitTrackBackend.Models;

public class Workout
{
    [Key]
    public int WorkoutId { get; set; }

    public int UserId { get; set; }

    public DateTime WorkoutDate { get; set; }

    public string Notes { get; set; } = "";

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}