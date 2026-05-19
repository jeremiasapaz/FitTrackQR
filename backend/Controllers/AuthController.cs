using FitTrackBackend.Data;
using FitTrackBackend.Models;
using Microsoft.AspNetCore.Mvc;

namespace FitTrackBackend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly AppDbContext _context;

    public AuthController(AppDbContext context)
    {
        _context = context;
    }

    [HttpPost("register")]
    public IActionResult Register(User user)
    {
        _context.Users.Add(user);
        _context.SaveChanges();

        return Ok(new
        {
            message = "User registered successfully"
        });
    }

    [HttpPost("login")]
    public IActionResult Login(User loginUser)
    {
        var user = _context.Users.FirstOrDefault(x =>
            x.Email == loginUser.Email &&
            x.PasswordHash == loginUser.PasswordHash);

        if (user == null)
        {
            return Unauthorized(new
            {
                message = "Invalid credentials"
            });
        }

        return Ok(new
        {
            message = "Login successful",
            userId = user.UserId,
            fullName = user.FullName,
            email = user.Email
        });
    }
}