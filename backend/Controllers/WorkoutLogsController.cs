using FitTrackBackend.Data;
using FitTrackBackend.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace FitTrackBackend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class WorkoutLogsController : ControllerBase
{
    private readonly AppDbContext _context;

    public WorkoutLogsController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<IActionResult> GetWorkoutLogs()
    {
        return Ok(await _context.WorkoutLogs.ToListAsync());
    }

    [HttpPost]
    public async Task<IActionResult> CreateWorkoutLog(WorkoutLog log)
    {
        _context.WorkoutLogs.Add(log);
        await _context.SaveChangesAsync();

        return Ok(log);
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteWorkoutLog(int id)
    {
        var log = await _context.WorkoutLogs.FindAsync(id);

        if (log == null)
        {
            return NotFound(new
            {
                message = "Workout log not found."
            });
        }

        _context.WorkoutLogs.Remove(log);
        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Workout log deleted successfully."
        });
    }
}