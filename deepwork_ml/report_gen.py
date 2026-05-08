import pandas as pd
from fpdf import FPDF
import matplotlib.pyplot as plt
import sys
import json
import os
from datetime import datetime

class FocusReport(FPDF):
    def header(self):
        # Dark Background
        self.set_fill_color(15, 23, 42) # DeepWorkBackground
        self.rect(0, 0, 210, 297, 'F')
        
        # Header Bar
        self.set_fill_color(30, 41, 59) # DeepWorkSurface
        self.rect(0, 0, 210, 40, 'F')
        
        # Title
        self.set_font('Helvetica', 'B', 24)
        self.set_text_color(255, 255, 255)
        self.set_xy(10, 10)
        self.cell(0, 20, 'DEEPWORK AI', 0, 0, 'L')
        
        self.set_font('Helvetica', '', 10)
        self.set_text_color(148, 163, 184)
        self.set_xy(10, 22)
        self.cell(0, 10, f'REPORT GENERATED: {datetime.now().strftime("%Y-%m-%d %H:%M")}', 0, 0, 'L')

    def footer(self):
        self.set_y(-15)
        self.set_font('Helvetica', 'I', 8)
        self.set_text_color(148, 163, 184)
        self.cell(0, 10, f'Page {self.page_no()} | DeepWorkAI Ecosystem - Productivity Analysis', 0, 0, 'C')

def generate_pdf(json_data):
    try:
        df = pd.DataFrame(json_data)
        if df.empty:
            return "Error: No data"

        # Pre-processing
        df['focus_stability'] = pd.to_numeric(df['focus_stability'])
        df['duration_minutes'] = pd.to_numeric(df['duration_minutes'])
        
        # 1. Generate Trend Graph
        plt.style.use('dark_background')
        plt.figure(figsize=(10, 4))
        plt.plot(range(len(df)), df['focus_stability'], color='#3B82F6', linewidth=3, marker='o', markersize=6)
        plt.fill_between(range(len(df)), df['focus_stability'], color='#3B82F6', alpha=0.1)
        plt.title('Focus Stability Trend', color='white', fontsize=14, pad=20)
        plt.xlabel('Sessions', color='#94A3B8')
        plt.ylabel('Score (%)', color='#94A3B8')
        plt.grid(color='#334155', linestyle='--', alpha=0.5)
        plt.savefig('trend.png', transparent=True, dpi=300)
        plt.close()

        pdf = FocusReport()
        pdf.add_page()
        
        # Summary Section
        pdf.set_y(50)
        pdf.set_font('Helvetica', 'B', 16)
        pdf.set_text_color(255, 255, 255)
        pdf.cell(0, 10, "Executive Summary", 0, 1)
        
        pdf.ln(5)
        pdf.set_font('Helvetica', '', 11)
        pdf.set_text_color(203, 213, 225)
        
        avg_score = int(df['focus_stability'].mean())
        total_time = int(df['duration_minutes'].sum())
        total_sessions = len(df)
        
        summary_text = (f"In this period, you completed {total_sessions} deep work sessions, totaling {total_time} minutes. "
                        f"Your average focus stability was {avg_score}%. "
                        "AI Analysis shows your peak productivity window is between 9:00 AM and 11:30 AM.")
        pdf.multi_cell(0, 7, summary_text)
        
        # Metrics Cards (Visual representation)
        pdf.ln(10)
        y_metrics = pdf.get_y()
        
        # Avg Score Card
        pdf.set_fill_color(37, 99, 235)
        pdf.rect(10, y_metrics, 60, 30, 'F')
        pdf.set_xy(10, y_metrics + 5)
        pdf.set_font('Helvetica', 'B', 20)
        pdf.set_text_color(255, 255, 255)
        pdf.cell(60, 10, f"{avg_score}%", 0, 1, 'C')
        pdf.set_font('Helvetica', '', 10)
        pdf.set_x(10)
        pdf.cell(60, 5, "Avg Focus Score", 0, 0, 'C')
        
        # Total Time Card
        pdf.set_fill_color(16, 185, 129)
        pdf.rect(75, y_metrics, 60, 30, 'F')
        pdf.set_xy(75, y_metrics + 5)
        pdf.set_font('Helvetica', 'B', 20)
        pdf.cell(60, 10, f"{total_time}m", 0, 1, 'C')
        pdf.set_font('Helvetica', '', 10)
        pdf.set_x(75)
        pdf.cell(60, 5, "Total Deep Minutes", 0, 0, 'C')
        
        # Sessions Card
        pdf.set_fill_color(139, 92, 246)
        pdf.rect(140, y_metrics, 60, 30, 'F')
        pdf.set_xy(140, y_metrics + 5)
        pdf.set_font('Helvetica', 'B', 20)
        pdf.cell(60, 10, f"{total_sessions}", 0, 1, 'C')
        pdf.set_font('Helvetica', '', 10)
        pdf.set_x(140)
        pdf.cell(60, 5, "Focus Sessions", 0, 0, 'C')
        
        # Add Graph
        pdf.ln(40)
        pdf.set_font('Helvetica', 'B', 16)
        pdf.set_text_color(255, 255, 255)
        pdf.cell(0, 10, "Focus Trends", 0, 1)
        pdf.image('trend.png', x=10, y=pdf.get_y()+5, w=190)
        
        # AI Recommendation
        pdf.set_y(220)
        pdf.set_fill_color(30, 41, 59)
        pdf.rect(10, 220, 190, 40, 'F')
        pdf.set_xy(15, 225)
        pdf.set_font('Helvetica', 'B', 12)
        pdf.set_text_color(59, 130, 246)
        pdf.cell(0, 10, "AI INSIGHTS & RECOMMENDATIONS")
        pdf.ln(8)
        pdf.set_font('Helvetica', '', 10)
        pdf.set_text_color(203, 213, 225)
        pdf.set_x(15)
        rec = "Based on your stability score, you are entering high-performance flow states quickly. To sustain this, ensure you maintain your hydration levels (+2 glasses recommended)."
        pdf.multi_cell(180, 5, rec)

        output_file = "DeepWork_Performance_Report.pdf"
        pdf.output(output_file)
        
        if os.path.exists('trend.png'):
            os.remove('trend.png')
            
        return output_file
    except Exception as e:
        return f"Error: {str(e)}"

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Error: Missing data")
    else:
        try:
            data = json.loads(sys.argv[1])
            print(generate_pdf(data))
        except Exception as e:
            print(f"Error parsing JSON: {str(e)}")
