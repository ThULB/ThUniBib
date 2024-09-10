const HisinOneProjects = {
    maxLength: 60,

    search: async function (text) {
        const q = await fetch("../rsc/project?q=" + text);
        let json = await q.json();

        let projects = [];
        json.forEach((project) => {
            let label = project.defaulttext;

            let item = {};
            item.id = project.id;
            item.label = (label.length > HisinOneProjects.maxLength ? label.substring(0, HisinOneProjects.maxLength).trim() + "…" : label) + " [" + project.shorttext + "]";
            item.defaultText = project.defaulttext;
            item.shortText = project.shorttext;
            projects.push(item);
        })

        return projects;
    },

    onProjectSelected: function (event, ui) {
        $("#project-search-his-in-one").val('');
        $("#project-id").val(ui.item.id);
        $("#project-title").val(ui.item.defaultText);
        $("#project-shorttext").val(ui.item.shortText);
        return false;
    },

    onInput: async function (event) {
        let input = $("#project-search-his-in-one");
        input.autocomplete({
            delay: 300,
            minLength: 0,
            source: await HisinOneProjects.search(event.target.value),
            select: HisinOneProjects.onProjectSelected
        });
    }
};

document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('project-search-his-in-one').addEventListener('input', HisinOneProjects.onInput);
});
