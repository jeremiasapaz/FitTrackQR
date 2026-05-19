using FitTrackBackend.Data;
using FitTrackBackend.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace FitTrackBackend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class WorkoutsController : ControllerBase
{
    private readonly AppDbContext _context;

    public WorkoutsController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<IActionResult> GetWorkouts()
    {
        return Ok(await _context.Workouts.ToListAsync());
    }

    [HttpPost]
    public async Task<IActionResult> CreateWorkout(Workout workout)
    {
        _context.Workouts.Add(workout);
        await _context.SaveChangesAsync();

        return Ok(workout);
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteWorkout(int id)
    {
        var workout = await _context.Workouts.FindAsync(id);

        if (workout == null)
        {
            return NotFound(new
            {
                message = "Workout not found."
            });
        }

        _context.Workouts.Remove(workout);
        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Workout deleted successfully."
        });
    }
}