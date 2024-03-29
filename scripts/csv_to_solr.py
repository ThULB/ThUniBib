import csv
import pysolr

def read_csv():
    project_data = []
    with open('201029_ThUniBib_Projekte.csv') as csv_file:
        reader = csv.reader(csv_file, delimiter=';')
        for line, row in enumerate(reader):
            if line == 0:
                print(row)
            else:
                data = {}
                data['project_id'] = row[0]
                data['acronym'] = row[1]
                data['project_title'] = row[2]
                data['funder'] = row[3]
                data['funding_number'] = row[4]
                data['project_search_all'] = ' '.join(row)
                project_data.append(data)
    return project_data

def update_solr(project_data_list):
    solr = pysolr.Solr('http://localhost:8983/solr/ubo-projects')
    solr.add(project_data_list)
    solr.commit()
        

if __name__ == '__main__':
    project_data = read_csv()
    update_solr(project_data)
