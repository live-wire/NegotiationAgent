import pandas as pd
import numpy as np
# import matplotlib.pyplot as plt
# from matplotlib import style
from collections import OrderedDict

# filename = input('Enter a file name:(without.csv) \n')
filename = 'party_result'
df = pd.read_csv(filename+'.csv',sep=';')
print df
# df = pd.read_csv('tournament-random0.6_timedependent2_min0.7.csv')
# print(df.head())
# print(df.columns.values)


# '#agreeing'
Valuable_df = df.drop(['Exception','deadline','Discounted','min.util.','max.util.','Profile 1','Profile 2','Profile 3'],1)

columns = ['Runtime','Round','Agreement','DisPareto','DisNash',
'Agent1','Agent2','Agent3','Utility1','Utility2','Utility3','DisU1','DisU2','DisU3']

Valuable_df.rename(columns = {'Run time (s)': 'Runtime','Dist. to Pareto' :'DisPareto', 'Dist. to Nash' :'DisNash','Agent 1' :'Agent_1',
	'Agent 2': 'Agent_2','Agent 3':'Agent_3','Agent 4':'Agent_4','Utility 1':'Util_1','Utility 2':'Util_2',
	'Utility 3':'Util_3','Utility 4':'Util_4','Disc. Util. 1':'DisUtil_1','Disc. Util. 2' :'DisUtil_2','Disc. Util. 3':'DisUtil_3','Disc. Util. 4':'DisUtil_4'}, inplace = True)
length_origin = len(Valuable_df)
Valuable_df.drop(Valuable_df[Valuable_df['Agreement']=='No'].index.values, inplace=True)
Valuable_df.reset_index(inplace = True)
length_after = len(Valuable_df)

# print(Valuable_df.columns.values)

num_of_not_agreed = length_origin - length_after
Social_Welfare_mean = Valuable_df['Social Welfare'].mean()
runtime_mean = Valuable_df['Runtime'].mean()
round_mean = Valuable_df['Round'].mean()
DisToPareto_mean = Valuable_df['DisPareto'].mean()
DisToNash_mean = Valuable_df['DisNash'].mean()

Agent_ID = []
Agent_util = [0,0,0,0]
Agents = []
count_utility_win_numbers = [0,0,0,0]

# Get Agents' ID
for k in range(0,5):
	for i in Valuable_df[['Agent_1','Agent_2','Agent_3']].loc[k]:
		if i.split('@')[0] not in Agent_ID:
			# Agent_ID.append(i.split('@')[0])
			Agent_ID.append(i.split('@')[0])

Agents_dic = {'ID':Agent_ID,'util_avg':[]}
# print("Agents_dic")
# print(Agents_dic['ID'])



arr = []
for index in range (0,len(Valuable_df)):
	arr.append(Valuable_df[['Util_1','Util_2','Util_3','Util_4']].loc[index])
	Agents.append(Valuable_df[['Agent_1','Agent_2','Agent_3','Agent_4']].loc[index])
	# print(np.argmax(arr[index]))
	# count_utility_win_numbers[] += 1


# print(arr[0])
# print(Agent_ID[3])
# print(Agents[0][3])

arr = np.array(arr)
for i in range(0,len(arr)):
	# Save Utilities to agents
	# print(i)
	for k in range(0,4):
		if Agent_ID[k] in Agents[i][0]:
			Agent_util[k]+= arr[i][0]
			# print("Add",Agents[i][0],"to",Agent_ID[k])
		elif Agent_ID[k] in Agents[i][1]:
			Agent_util[k]+= arr[i][1]
			# print("Add",Agents[i][1],"to",Agent_ID[k])
		elif Agent_ID[k] in Agents[i][2]:
			Agent_util[k]+= arr[i][2]
			# print("Add",Agents[i][2],"to",Agent_ID[k])
		elif Agent_ID[k] in Agents[i][3]:
			Agent_util[k]+= arr[i][3]
			# print("Add",Agents[i][3],"to",Agent_ID[k])



	if Agent_ID[0] in Agents[i][np.argmax(arr[i])]:
		count_utility_win_numbers[0] += 1
	elif Agent_ID[1] in Agents[i][np.argmax(arr[i])]:
		count_utility_win_numbers[1] += 1
	elif Agent_ID[2] in Agents[i][np.argmax(arr[i])]:
		count_utility_win_numbers[2] += 1
	elif Agent_ID[3] in Agents[i][np.argmax(arr[i])]:
		count_utility_win_numbers[3] += 1


for i in range(0,len(Agent_util)):
	Agent_util[i] = Agent_util[i]/len(arr)



Agents_dic['util_avg'] = Agent_util
# print(Agents_dic)
# print(count_utility_win_numbers)



# print("")
# print("Mean run_time is :",runtime_mean)
# print("Mean round is :",round_mean)
# print("Mean distance to Pareto is :",DisToPareto_mean)
# print("Mean distance to Nash is :",DisToNash_mean)
# print("")

output_columns = {'Runtime_avg':[],
				'rounds_avg':[],
				str(Agents_dic['ID'][0])+'_wins':[],
				str(Agents_dic['ID'][1])+'_wins':[],
				str(Agents_dic['ID'][2])+'_wins':[],
				str(Agents_dic['ID'][3])+'_wins':[],
				str(Agents_dic['ID'][0])+'_Util_avg':[],
				str(Agents_dic['ID'][1])+'_Util_avg':[],
				str(Agents_dic['ID'][2])+'_Util_avg':[],
				str(Agents_dic['ID'][3])+'_Util_avg':[],
				'Dis_to_Pareto':[],
				'Dis_to_Nash':[],
				'Social_Welfare':[],
				'NoAgreement_num':[],
				'FileName':[]}

DataResult = pd.DataFrame(OrderedDict(output_columns))


# print(DataResult.columns.values)

DataResult.loc[0] = [runtime_mean,
					round_mean,
					count_utility_win_numbers[0],
					count_utility_win_numbers[1],
					count_utility_win_numbers[2],
					count_utility_win_numbers[3],
					Agents_dic['util_avg'][0],
					Agents_dic['util_avg'][1],
					Agents_dic['util_avg'][2],
					Agents_dic['util_avg'][3],
					DisToPareto_mean,
					DisToNash_mean,
					Social_Welfare_mean,
					num_of_not_agreed,
					filename]
# print(DataResult.head())
try:
	DataResult_origin = pd.read_csv('See.csv',index_col=0)
	DataResult= pd.concat([DataResult_origin,DataResult],ignore_index=True)
	print("Data_merged")
except Exception as e:
	print("")


DataResult.to_csv('See.csv')




