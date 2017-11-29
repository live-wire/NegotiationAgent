import numpy as np
import pandas as pd

df = pd.read_csv('party_result2.csv',sep=';')

Agents = ['Group19','Atlas3','AgentW','AgentBuyogMain']
AgentUtilSums = {}

for agent in Agents:
    AgentUtilSums[agent] = 0.0

iterations = 0
agreements = 0

for index, row in df.iterrows():
    iterations = iterations + 1
    if row['Agreement'] == 'Yes':
        agreements = agreements+1
        for agent in Agents:
            for it in range(1,5): # Agent 1-4 , Utility 1-4
                if agent in row['Agent '+str(it)]:
                    AgentUtilSums[agent] = AgentUtilSums[agent] + row['Utility '+str(it)]


print "Agreements=",agreements,"Iterations=",iterations
print "Average Utilities: \n"
for agent in Agents:
    print agent,AgentUtilSums[agent]/agreements
    print "\n"
