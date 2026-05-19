using System.ComponentModel.DataAnnotations;

namespace FitTrackBackend.Models;

public class User
{
    [Key]
    public int UserId { get; set; }

    public string FullName { get; set; } = "";

    public string Email { get; set; } = "";

    public string PasswordHash { get; set; } = "";
    
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}