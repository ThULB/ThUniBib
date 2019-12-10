import csv
import pysolr

def read_csv():
    project_data = []
    with open('191121_EU-Projekte.csv') as csv_file:
        reader = csv.reader(csv_file, delimiter=';')
        for line, row in enumerate(reader):
            if line == 0:
                print(row)
            else:
                data = {}
                data['projekt_id'] = row[0]
                data['acronym'] = row[1]
                data['projekttitel'] = row[2]
                data['kostentraeger'] = row[3]
                data['foerderkennzeichen'] = row[4]
                data['project_search_all'] = ' '.join(row)
                project_data.append(data)
    return project_data

def update_solr(project_data_list):
    solr = pysolr.Solr('http://localhost:8081/solr/ubo_projects')
    solr.add(project_data_list)
    solr.commit()
        

if __name__ == '__main__':
    project_data = read_csv()
    update_solr(project_data)