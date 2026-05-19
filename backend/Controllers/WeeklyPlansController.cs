using FitTrackBackend.Data;
using FitTrackBackend.DTOs;
using FitTrackBackend.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace FitTrackBackend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class WeeklyPlansController : ControllerBase
{
    private readonly AppDbContext _context;

    public WeeklyPlansController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<IActionResult> GetWeeklyPlans()
    {
        return Ok(await _context.WeeklyPlans.ToListAsync());
    }

    [HttpGet("user/{userId}")]
    public async Task<IActionResult> GetWeeklyPlansByUser(int userId)
    {
        var plans = await _context.WeeklyPlans
            .Where(p => p.UserId == userId)
            .ToListAsync();

        return Ok(plans);
    }

    [HttpPost]
    public async Task<IActionResult> CreateWeeklyPlan(CreateWeeklyPlanRequest request)
    {
        if (request.UserId <= 0)
        {
            return BadRequest(new { message = "Invalid user ID." });
        }

        if (string.IsNullOrWhiteSpace(request.DayOfWeek))
        {
            return BadRequest(new { message = "Day of week is required." });
        }

        if (request.ExerciseIds == null || request.ExerciseIds.Count == 0)
        {
            return BadRequest(new { message = "At least one exercise must be selected." });
        }

        var validDays = new[]
        {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
        };

        if (!validDays.Contains(request.DayOfWeek))
        {
            return BadRequest(new { message = "Invalid day of week." });
        }

        var weeklyPlans = request.ExerciseIds.Select(exerciseId => new WeeklyPlan
        {
            UserId = request.UserId,
            DayOfWeek = request.DayOfWeek,
            ExerciseId = exerciseId,
            CreatedAt = DateTime.UtcNow
        }).ToList();

        _context.WeeklyPlans.AddRange(weeklyPlans);
        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Weekly plan created successfully.",
            plans = weeklyPlans
        });
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteWeeklyPlan(int id)
    {
        var plan = await _context.WeeklyPlans.FindAsync(id);

        if (plan == null)
        {
            return NotFound(new
            {
                message = "Weekly plan not found."
            });
        }

        _context.WeeklyPlans.Remove(plan);
        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Weekly plan deleted successfully."
        });
    }
}