using System.ComponentModel.DataAnnotations;

namespace FitTrackBackend.Models;

public class Exercise
{
    [Key]
    public int ExerciseId { get; set; }

    public string ExerciseName { get; set; } = "";

    public string MuscleGroup { get; set; } = "";

    public string AliasName { get; set; } = "";

    public string QrCode { get; set; } = "";
    
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}