package utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import models.Stock;
import service.StockService;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Automatic stock monitoring service that checks stock levels at regular intervals
 * and sends email alerts when items are running low
 */
public class AutoStockMonitor {
    private static final Logger LOGGER = Logger.getLogger(AutoStockMonitor.class.getName());
    
    // Default check interval in milliseconds (default: 1 hour)
    private static final long DEFAULT_CHECK_INTERVAL = 60 * 60 * 1000;
    
    // Default recipient for email alerts
    private static final String DEFAULT_RECIPIENT = "manager@fruitables.com";
    
    private final StockService stockService;
    private final EmailService emailService;
    private final Timer timer;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    private long checkInterval;
    private String emailRecipient;
    
    /**
     * Create a new automatic stock monitor with default settings
     */
    public AutoStockMonitor() {
        this(DEFAULT_CHECK_INTERVAL, DEFAULT_RECIPIENT);
    }
    
    /**
     * Create a new automatic stock monitor with custom settings
     * 
     * @param checkInterval Interval between stock checks in milliseconds
     * @param emailRecipient Email address to receive low stock alerts
     */
    public AutoStockMonitor(long checkInterval, String emailRecipient) {
        this.checkInterval = checkInterval;
        this.emailRecipient = emailRecipient;
        this.stockService = new StockService();
        this.emailService = new EmailService();
        this.timer = new Timer(true); // Run as daemon
    }
    
    /**
     * Start monitoring stock levels automatically
     */
    public void startMonitoring() {
        if (isRunning.compareAndSet(false, true)) {
            LOGGER.info("Starting automatic stock monitoring");
            
            // Schedule the first check immediately
            checkStockLevels();
            
            // Schedule recurring checks
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    checkStockLevels();
                }
            }, checkInterval, checkInterval);
        }
    }
    
    /**
     * Stop monitoring stock levels
     */
    public void stopMonitoring() {
        if (isRunning.compareAndSet(true, false)) {
            LOGGER.info("Stopping automatic stock monitoring");
            timer.cancel();
        }
    }
    
    /**
     * Set the interval between stock checks
     * 
     * @param interval Interval in milliseconds
     */
    public void setCheckInterval(long interval) {
        this.checkInterval = interval;
        if (isRunning.get()) {
            // Restart monitoring with new interval
            stopMonitoring();
            startMonitoring();
        }
    }
    
    /**
     * Set the email recipient for low stock alerts
     * 
     * @param recipient Email address
     */
    public void setEmailRecipient(String recipient) {
        this.emailRecipient = recipient;
    }
    
    /**
     * Check stock levels and send email alert if necessary
     */
    private void checkStockLevels() {
        try {
            LOGGER.info("Checking stock levels automatically");
            
            List<Stock> lowStockItems = stockService.findLowStocks();
            
            if (!lowStockItems.isEmpty()) {
                LOGGER.warning("Found " + lowStockItems.size() + " low stock items");
                
                // Send email alert
                boolean success = emailService.sendLowStockAlert(emailRecipient, lowStockItems);
                
                if (success) {
                    LOGGER.info("Automatic low stock email alert sent successfully to " + emailRecipient);
                    
                    // Show a notification in the UI (if application is running)
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Low Stock Alert");
                        alert.setHeaderText("Automatic Low Stock Alert Sent");
                        alert.setContentText("A low stock alert email has been automatically sent to " + emailRecipient);
                        alert.show();
                    });
                } else {
                    LOGGER.warning("Failed to send automatic low stock email alert");
                }
            } else {
                LOGGER.info("No low stock items found");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking stock levels: " + e.getMessage(), e);
        }
    }
} 