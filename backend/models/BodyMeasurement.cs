using System.ComponentModel.DataAnnotations;

namespace FitTrackBackend.Models;

public class BodyMeasurement
{
    [Key]
    public int MeasurementId { get; set; }

    public int UserId { get; set; }

    public decimal BodyWeight { get; set; }

    public decimal Chest { get; set; }

    public decimal Arms { get; set; }

    public decimal Waist { get; set; }

    public decimal Legs { get; set; }

    public DateTime MeasurementDate { get; set; }
}