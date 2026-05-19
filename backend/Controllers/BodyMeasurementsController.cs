using FitTrackBackend.Data;
using FitTrackBackend.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace FitTrackBackend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class BodyMeasurementsController : ControllerBase
{
    private readonly AppDbContext _context;

    public BodyMeasurementsController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<IActionResult> GetMeasurements()
    {
        return Ok(await _context.BodyMeasurements.ToListAsync());
    }

    [HttpGet("user/{userId}")]
    public async Task<IActionResult> GetMeasurementsByUserId(int userId)
    {
        var measurements = await _context.BodyMeasurements
            .Where(m => m.UserId == userId)
            .OrderByDescending(m => m.MeasurementDate)
            .ThenByDescending(m => m.MeasurementId)
            .ToListAsync();

        return Ok(measurements);
    }

    [HttpPost]
    public async Task<IActionResult> CreateMeasurement(BodyMeasurement measurement)
    {
        measurement.MeasurementDate = DateTime.SpecifyKind(
            measurement.MeasurementDate,
            DateTimeKind.Utc
        );

        _context.BodyMeasurements.Add(measurement);
        await _context.SaveChangesAsync();

        return Ok(measurement);
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateMeasurement(int id, BodyMeasurement measurement)
    {
        var existingMeasurement = await _context.BodyMeasurements.FindAsync(id);

        if (existingMeasurement == null)
        {
            return NotFound(new
            {
                message = "Body measurement not found."
            });
        }

        existingMeasurement.UserId = measurement.UserId;
        existingMeasurement.BodyWeight = measurement.BodyWeight;
        existingMeasurement.Chest = measurement.Chest;
        existingMeasurement.Arms = measurement.Arms;
        existingMeasurement.Waist = measurement.Waist;
        existingMeasurement.Legs = measurement.Legs;
        existingMeasurement.MeasurementDate = DateTime.SpecifyKind(
            measurement.MeasurementDate,
            DateTimeKind.Utc
        );

        await _context.SaveChangesAsync();

        return Ok(existingMeasurement);
    }
}