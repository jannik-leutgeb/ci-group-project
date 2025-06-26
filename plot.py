import csv
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter

# Read the CSV data
rounds = []
scores = []

with open('data.csv', 'r') as csvfile:
    reader = csv.DictReader(csvfile)
    for row in reader:
        rounds.append(int(row['ROUND']))
        scores.append(int(row['SCORE']))

def thousands_formatter(x, pos):
    return f'{int(x/1000)}k'

# Plot the data
plt.figure(figsize=(16, 5))
plt.plot(rounds, scores, linestyle='-', color='blue', label='Cost per round')
plt.title('Cost reduction')
plt.xlabel('Round')
plt.ylabel('Score')
plt.gca().yaxis.set_major_formatter(FuncFormatter(thousands_formatter))
plt.grid(True)
plt.legend()
plt.tight_layout()
#plt.show()
plt.savefig("score_plot.png", format='png', dpi=600, bbox_inches='tight')
