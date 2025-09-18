// Revenue Chart Management
class RevenueChart {
    constructor() {
        this.chart = null;
        this.initialData = {
            monthlyStats: [],
            monthlyRevenue: 0,
            monthlyBookings: 0,
            currentYear: new Date().getFullYear(),
            currentMonth: new Date().getMonth() + 1
        };
        
        this.init();
    }
    
    // Initialize with data from server
    initWithData(monthlyStats, monthlyRevenue, monthlyBookings, currentYear, currentMonth) {
        this.initialData = {
            monthlyStats: monthlyStats || [],
            monthlyRevenue: monthlyRevenue || 0,
            monthlyBookings: monthlyBookings || 0,
            currentYear: currentYear || new Date().getFullYear(),
            currentMonth: currentMonth || new Date().getMonth() + 1
        };
        
        console.log('RevenueChart initialized with data:', this.initialData);
    }
    
    // Initialize chart
    init() {
        console.log('Initializing RevenueChart...');
        
        // Check if we have initial data
        if (!this.initialData.monthlyStats || this.initialData.monthlyStats.length === 0) {
            console.log('No initial data found, loading data for current month...');
            this.loadChartData(this.initialData.currentYear, this.initialData.currentMonth);
        } else {
            console.log('Initial data found, creating chart...');
            this.createChart();
        }
        
        this.setupEventListeners();
    }
    
    // Create the chart
    createChart() {
        const ctx = document.getElementById('revenueChart').getContext('2d');
        
        // Debug: Check initial data
        console.log('=== INITIAL DATA DEBUG ===');
        console.log('Current Year:', this.initialData.currentYear);
        console.log('Current Month:', this.initialData.currentMonth);
        console.log('Initial Monthly Stats:', this.initialData.monthlyStats);
        console.log('Initial Monthly Revenue:', this.initialData.monthlyRevenue);
        console.log('Initial Monthly Bookings:', this.initialData.monthlyBookings);
        console.log('==========================');
        
        // Create chart data
        const chartData = this.createChartData(
            this.initialData.currentYear, 
            this.initialData.currentMonth, 
            this.initialData.monthlyStats
        );
        
        console.log('Chart Data Created:', chartData);
        
        this.chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: chartData.labels,
                datasets: [{
                    label: 'Doanh thu (VNĐ)',
                    data: chartData.revenue,
                    backgroundColor: chartData.revenue.map(value => 
                        value > 0 ? 'rgba(102, 126, 234, 0.8)' : 'rgba(220, 220, 220, 0.3)'
                    ),
                    borderColor: chartData.revenue.map(value => 
                        value > 0 ? 'rgba(102, 126, 234, 1)' : 'rgba(220, 220, 220, 0.5)'
                    ),
                    borderWidth: 1,
                    borderRadius: 4,
                    maxBarThickness: 20
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { 
                        display: false 
                    },
                    tooltip: {
                        callbacks: {
                            label: (context) => {
                                const value = context.parsed.y;
                                if (value > 0) {
                                    return `Ngày ${context.label}: ${this.formatCurrency(value)}`;
                                } else {
                                    return `Ngày ${context.label}: Không có doanh thu`;
                                }
                            }
                        },
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        titleColor: 'white',
                        bodyColor: 'white',
                        borderColor: 'rgba(255, 255, 255, 0.2)',
                        borderWidth: 1
                    }
                },
                scales: {
                    x: { 
                        grid: { display: false },
                        ticks: {
                            maxRotation: 45,
                            minRotation: 0,
                            font: {
                                size: 10
                            }
                        }
                    },
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.1)'
                        },
                        ticks: {
                            callback: (value) => this.formatCurrency(value),
                            font: {
                                size: 10
                            }
                        }
                    }
                },
                interaction: {
                    intersect: false,
                    mode: 'index'
                }
            }
        });
        
        // Update stats
        this.updateStats(this.initialData.monthlyRevenue, this.initialData.monthlyBookings);
    }
    
    // Create chart data from stats
    createChartData(year, month, stats) {
        console.log('Creating chart data for:', year, month, 'with stats:', stats);
        
        const daysInMonth = new Date(year, month, 0).getDate();
        const labels = [];
        const revenue = [];
        
        // Create map for easy lookup by day
        const statsMap = new Map();
        
        if (stats && Array.isArray(stats) && stats.length > 0) {
            console.log('Processing stats array with length:', stats.length);
            stats.forEach((stat, index) => {
                console.log(`Processing stat ${index}:`, stat);
                
                let day;
                if (stat.date && typeof stat.date === 'object') {
                    if (stat.date.dayOfMonth) {
                        day = stat.date.dayOfMonth;
                    } else if (stat.date.day) {
                        day = stat.date.day;
                    } else {
                        console.log('Skipping stat - no valid day found:', stat);
                        return;
                    }
                } else if (typeof stat.date === 'string') {
                    // Parse date string like '2025-09-15'
                    const dateParts = stat.date.split('-');
                    day = parseInt(dateParts[2]);
                } else {
                    console.log('Skipping stat - invalid date format:', stat);
                    return;
                }
                
                let revenueValue = 0;
                if (stat.revenue !== null && stat.revenue !== undefined) {
                    if (typeof stat.revenue === 'number') {
                        revenueValue = stat.revenue;
                    } else if (typeof stat.revenue === 'string') {
                        revenueValue = parseFloat(stat.revenue) || 0;
                    } else if (stat.revenue && typeof stat.revenue === 'object') {
                        if (stat.revenue.value !== undefined) {
                            revenueValue = parseFloat(stat.revenue.value) || 0;
                        } else if (stat.revenue.scale !== undefined && stat.revenue.unscaledValue !== undefined) {
                            const unscaledValue = parseFloat(stat.revenue.unscaledValue) || 0;
                            const scale = parseInt(stat.revenue.scale) || 0;
                            revenueValue = unscaledValue / Math.pow(10, scale);
                        }
                    }
                }
                
                console.log(`Day ${day}: Revenue = ${revenueValue}`);
                
                if (day >= 1 && day <= daysInMonth) {
                    const existingRevenue = statsMap.get(day) || 0;
                    statsMap.set(day, existingRevenue + revenueValue);
                    console.log(`✅ Day ${day}: ${existingRevenue} + ${revenueValue} = ${statsMap.get(day)}`);
                } else {
                    console.warn(`❌ Day ${day} is out of range for month with ${daysInMonth} days`);
                }
            });
        } else {
            console.log('No stats data available or invalid format');
        }
        
        // Create array for all days in month
        for (let i = 1; i <= daysInMonth; i++) {
            labels.push(`Ngày ${i}`);
            revenue.push(statsMap.get(i) || 0);
        }
        
        console.log('Final chart data - labels:', labels.length, 'revenue:', revenue);
        console.log('Non-zero revenue values:', revenue.filter(r => r > 0));
        
        return { labels, revenue };
    }
    
    // Format currency
    formatCurrency(value) {
        if (value >= 1000000) {
            return (value / 1000000).toFixed(1) + 'M VNĐ';
        } else if (value >= 1000) {
            return (value / 1000).toFixed(0) + 'K VNĐ';
        } else {
            return value.toLocaleString('vi-VN') + ' VNĐ';
        }
    }
    
    // Update stats display
    updateStats(revenue, bookings) {
        const revenueElement = document.getElementById('monthlyRevenueDisplay');
        const bookingsElement = document.getElementById('monthlyBookingsDisplay');
        
        if (revenueElement) {
            revenueElement.textContent = this.formatCurrency(revenue);
        }
        if (bookingsElement) {
            bookingsElement.textContent = bookings.toLocaleString('vi-VN');
        }
    }
    
    // Load chart data via API
    loadChartData(year, month) {
        // Show loading
        this.updateStats(0, 0);
        document.getElementById('monthlyRevenueDisplay').textContent = 'Loading...';
        document.getElementById('monthlyBookingsDisplay').textContent = 'Loading...';
        
        fetch(`/admin/dashboard/monthly-stats?year=${year}&month=${month}`)
            .then(response => response.json())
            .then(data => {
                console.log('Chart data received:', data);
                
                // Create chart data
                const chartData = this.createChartData(year, month, data.monthlyStats);
                
                // Update or create chart
                if (this.chart) {
                    // Update existing chart
                    this.chart.data.labels = chartData.labels;
                    this.chart.data.datasets[0].data = chartData.revenue;
                    this.chart.data.datasets[0].backgroundColor = chartData.revenue.map(value => 
                        value > 0 ? 'rgba(102, 126, 234, 0.8)' : 'rgba(220, 220, 220, 0.3)'
                    );
                    this.chart.data.datasets[0].borderColor = chartData.revenue.map(value => 
                        value > 0 ? 'rgba(102, 126, 234, 1)' : 'rgba(220, 220, 220, 0.5)'
                    );
                    this.chart.update();
                } else {
                    // Create new chart
                    this.createChartWithData(chartData);
                }
                
                // Update stats
                this.updateStats(data.monthlyRevenue, data.monthlyBookings);
            })
            .catch(error => {
                console.error('Error loading chart data:', error);
                this.updateStats(0, 0);
                document.getElementById('monthlyRevenueDisplay').textContent = 'Error';
                document.getElementById('monthlyBookingsDisplay').textContent = 'Error';
            });
    }
    
    // Create chart with specific data
    createChartWithData(chartData) {
        const ctx = document.getElementById('revenueChart').getContext('2d');
        this.chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: chartData.labels,
                datasets: [{
                    label: 'Doanh thu (VNĐ)',
                    data: chartData.revenue,
                    backgroundColor: chartData.revenue.map(value => 
                        value > 0 ? 'rgba(102, 126, 234, 0.8)' : 'rgba(220, 220, 220, 0.3)'
                    ),
                    borderColor: chartData.revenue.map(value => 
                        value > 0 ? 'rgba(102, 126, 234, 1)' : 'rgba(220, 220, 220, 0.5)'
                    ),
                    borderWidth: 1,
                    borderRadius: 4,
                    maxBarThickness: 20
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { 
                        display: false 
                    },
                    tooltip: {
                        callbacks: {
                            label: (context) => {
                                const value = context.parsed.y;
                                if (value > 0) {
                                    return `Ngày ${context.label}: ${this.formatCurrency(value)}`;
                                } else {
                                    return `Ngày ${context.label}: Không có doanh thu`;
                                }
                            }
                        },
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        titleColor: 'white',
                        bodyColor: 'white',
                        borderColor: 'rgba(255, 255, 255, 0.2)',
                        borderWidth: 1
                    }
                },
                scales: {
                    x: { 
                        grid: { display: false },
                        ticks: {
                            maxRotation: 45,
                            minRotation: 0,
                            font: {
                                size: 10
                            }
                        }
                    },
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.1)'
                        },
                        ticks: {
                            callback: (value) => this.formatCurrency(value),
                            font: {
                                size: 10
                            }
                        }
                    }
                },
                interaction: {
                    intersect: false,
                    mode: 'index'
                }
            }
        });
    }
    
    // Setup event listeners
    setupEventListeners() {
        const yearSelect = document.getElementById('chartYearSelect');
        const monthSelect = document.getElementById('chartMonthSelect');
        
        if (yearSelect) {
            yearSelect.addEventListener('change', () => {
                const year = parseInt(yearSelect.value);
                const month = parseInt(monthSelect.value);
                this.loadChartData(year, month);
            });
        }
        
        if (monthSelect) {
            monthSelect.addEventListener('change', () => {
                const year = parseInt(yearSelect.value);
                const month = parseInt(monthSelect.value);
                this.loadChartData(year, month);
            });
        }
    }
}

// Global instance
let revenueChartInstance = null;

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM Content Loaded - Initializing revenue chart...');
    
    // Get data from server (passed via Thymeleaf)
    const initialMonthlyStats = window.initialMonthlyStats || [];
    const initialMonthlyRevenue = window.initialMonthlyRevenue || 0;
    const initialMonthlyBookings = window.initialMonthlyBookings || 0;
    const currentYear = window.currentYear || new Date().getFullYear();
    const currentMonth = window.currentMonth || new Date().getMonth() + 1;
    
    // Create and initialize chart
    revenueChartInstance = new RevenueChart();
    revenueChartInstance.initWithData(
        initialMonthlyStats, 
        initialMonthlyRevenue, 
        initialMonthlyBookings, 
        currentYear, 
        currentMonth
    );
});
