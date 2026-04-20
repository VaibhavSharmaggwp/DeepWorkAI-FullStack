import pandas as pd
from fpdf import FPDF
import matplotlib.pyplot as plt
import sys
import json

class FocusReport(FPDF):
    def header(self):
        self.set_fill_color(13, 17, 23) # App Dark Background
        self.rect(0, 0, 210, 297, 'F')
        self.set_font('Arial', 'B', 20)
        self.set_text_color(255, 255, 255)
        self.cell(0, 20, 'DeepWork AI: Monthly Performance', 0, 1, 'C')

def generate_pdf(json_data):
    df = pd.DataFrame(json_data)
    
    # 1. Generate a Trend Graph
    plt.figure(figsize=(10, 5))
    plt.plot(df['start_time'], df['focus_stability'], color='#2563EB', linewidth=3)
    plt.axis('off')
    plt.savefig('trend.png', transparent=True)

    pdf = FocusReport()
    pdf.add_page()
    
    # 2. Add Metrics
    pdf.set_font('Arial', '', 12)
    pdf.set_text_color(148, 163, 184)
    avg_score = int(df['focus_stability'].mean())
    pdf.cell(0, 10, f"Average Monthly Focus: {avg_score}%", 0, 1)
    
    # 3. Add AI Insights
    pdf.image('trend.png', x=10, y=50, w=190)
    pdf.output("Focus_Report.pdf")
    return "Focus_Report.pdf"

if __name__ == "__main__":
    data = json.loads(sys.argv[1])
    print(generate_pdf(data))
